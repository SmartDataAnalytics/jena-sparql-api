package org.aksw.jena_sparql_api.http.repository.impl;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.dcat.ap.domain.api.Checksum;
import org.aksw.jena_sparql_api.conjure.algebra.common.ResourceTreeUtils;
import org.aksw.jena_sparql_api.conjure.entity.algebra.Op;
import org.aksw.jena_sparql_api.conjure.entity.engine.OpExecutor;
import org.aksw.jena_sparql_api.conjure.entity.engine.Planner;
import org.aksw.jena_sparql_api.conjure.entity.utils.PathCoderRegistry;
import org.aksw.jena_sparql_api.conjure.utils.ContentTypeUtils;
import org.aksw.jena_sparql_api.conjure.utils.FileUtils;
import org.aksw.jena_sparql_api.conjure.utils.HttpHeaderUtils;
import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;
import org.aksw.jena_sparql_api.http.repository.api.HttpResourceRepositoryFromFileSystem;
import org.aksw.jena_sparql_api.http.repository.api.RdfHttpEntity;
import org.aksw.jena_sparql_api.http.repository.api.RdfHttpEntityFile;
import org.aksw.jena_sparql_api.http.repository.api.RdfHttpResourceFile;
import org.aksw.jena_sparql_api.http.repository.api.ResourceStore;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpRequest;
import org.apache.jena.ext.com.google.common.base.Splitter; // due to spark conflict
import org.apache.jena.ext.com.google.common.hash.HashCode; // due to spark conflict
import org.apache.jena.ext.com.google.common.hash.Hashing;  // due to spark conflict
import org.apache.jena.ext.com.google.common.io.ByteSource; // due to spark conflict
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sys.JenaSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.StandardSystemProperty;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.net.MediaType;

public class HttpResourceRepositoryFromFileSystemImpl
    implements HttpResourceRepositoryFromFileSystem
{
    private static final Logger logger = LoggerFactory.getLogger(HttpResourceRepositoryFromFileSystemImpl.class);

    protected ResourceStore downloadStore;
    protected ResourceStore cacheStore;
    protected ResourceStore hashStore;

    public HttpResourceRepositoryFromFileSystemImpl() {
        super();
    }

    /**
     * Utility method to convert a hash value (e.g. sha256) to a relative path
     * by splitting after 'n' characters
     *
     * @param hash
     * @return
     */
    public static Path hashToRelPath(String hash) {
        List<String> parts = Splitter
                .fixedLength(8)
                .splitToList(hash);

        String id = parts.stream()
                .collect(Collectors.joining("/"));

        Path result = Paths.get(id);
        return result;
    }

    public static HttpResourceRepositoryFromFileSystemImpl create(Path absBasePath) {
        HttpResourceRepositoryFromFileSystemImpl result = new HttpResourceRepositoryFromFileSystemImpl();
        result.setDownloadStore(new ResourceStoreImpl(absBasePath.resolve("downloads")));
        result.setCacheStore(new ResourceStoreImpl(absBasePath.resolve("cache")));

        result.setHashStore(new ResourceStoreImpl(absBasePath.resolve("hash"), HttpResourceRepositoryFromFileSystemImpl::hashToRelPath));

        return result;
    }


    public Collection<ResourceStore> getResourceStores() {
        return Arrays.asList(downloadStore, cacheStore, hashStore);
    }

    public ResourceStore getDownloadStore() {
        return downloadStore;
    }

    public void setDownloadStore(ResourceStore downloadStore) {
        this.downloadStore = downloadStore;
    }

    public ResourceStore getCacheStore() {
        return cacheStore;
    }

    public void setCacheStore(ResourceStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    public ResourceStore getHashStore() {
        return hashStore;
    }

    public void setHashStore(ResourceStore hashStore) {
        this.hashStore = hashStore;
    }


    public ResourceStore getStoreByPath(Path path) {
        ResourceStore result = getResourceStores().stream()
            .filter(store -> store.contains(path))
            .findFirst()
            .orElse(null);

        return result;
    }

    public Resource getInfo(Path path) {
        Resource result = Optional.ofNullable(getStoreByPath(path)).map(store -> store.getInfo(path))
                .orElse(null);
        return result;
    }


    public List<Path> readSymbolicLinkTransitive(Path absPath) {

        Set<Path> seen = new LinkedHashSet<>();

        // tentative result list, may be set to null
        // if the link resolves to a non-existent target
        List<Path> result = new ArrayList<>();

        while(Files.isSymbolicLink(absPath)) {
            if(!Files.exists(absPath)) {
                result = null;
                break;
            }

            if(seen.contains(absPath)) {
                throw new RuntimeException("Cyclic symbolic link detected: " + seen);
            }
            seen.add(absPath);

            result.add(absPath);

            Path tmpPath;
            try {
                tmpPath = Files.readSymbolicLink(absPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            absPath = absPath.getParent().resolve(tmpPath).normalize();
        }

        if(result != null) {
            result.add(absPath);
            Collections.reverse(result);
        }

        return result;
    }


    // Assumes that there is at most 1 repository associated with a given path
    public RdfHttpEntityFile getEntityForPath(Path path) {
        List<Path> resolvedPaths = readSymbolicLinkTransitive(path);
        Collection<ResourceStore> stores = getResourceStores();

        RdfHttpEntityFile result = null;

        outer: for(Path resolvedPath : resolvedPaths) {
            for(ResourceStore store : stores) {
                result = store.getEntityForPath(resolvedPath);
                if(result != null) {
                    break outer;
                }
            }
        }

        return result;
    }

    public Collection<RdfHttpEntityFile> getEntities(String uri) {
        Collection<ResourceStore> stores = getResourceStores();

        Collection<RdfHttpEntityFile> result = stores.stream()
                .map(store -> store.getResource(uri))
                .flatMap(res -> res.getEntities().stream())
                //.flatMap(store -> store.listEntities(relPath).stream())
                .collect(Collectors.toList());
        return result;
    }

    public static BasicHttpRequest createRequest(String url, String contentType, List<String> encodings) {
        BasicHttpRequest result = new BasicHttpRequest("GET", url);
        result.setHeader(HttpHeaders.ACCEPT, contentType);

        List<String> effectiveEncodings = new ArrayList<>(encodings);
        if(!encodings.contains(IDENTITY_ENCODING) && !encodings.isEmpty()) {
            effectiveEncodings.add("identity;q=0");
        }

        String encoding = effectiveEncodings.stream().collect(Collectors.joining(","));

        result.setHeader(HttpHeaders.ACCEPT_ENCODING, encoding);

        return result;
    }

    /**
     * Convenience method for requesting a resource with given content type and encodingsS
     * @param url
     * @param contentType
     * @param encodings
     * @return
     * @throws IOException
     */
    public static RdfHttpEntityFile get(HttpResourceRepositoryFromFileSystem repo, String url, String contentType, List<String> encodings) throws IOException {
        BasicHttpRequest request = createRequest(url, contentType, encodings);

        RdfHttpEntityFile result = repo.get(request, HttpResourceRepositoryFromFileSystemImpl::resolveRequest);
        return result;
    }


    public String bestEncoding(Collection<String> encodings) {
        PathCoderRegistry registry = PathCoderRegistry.get();
        String result = encodings.stream()
            .filter(enc -> registry.getCoder(enc) != null)
            .findFirst()
            .orElse(null);

        return result;
    }

    public MediaType bestContentType(Collection<MediaType> contentTypes) {
        List<MediaType> supportedMediaTypes = HttpHeaderUtils.supportedMediaTypes();

        MediaType result = contentTypes.stream()
            .flatMap(range -> supportedMediaTypes.stream()
                    .filter(supportedMt -> supportedMt.is(range)))
            .findFirst()
            .orElse(null);

        return result;
    }

    public static final String IDENTITY_ENCODING = "identity";

    static class Plan {
        protected Op op;
        protected RdfEntityInfo info;

        public Plan(Op op, RdfEntityInfo info) {
            super();
            this.op = op;
            this.info = info;
        }

        public Op getOp() {
            return op;
        }

        public RdfEntityInfo getInfo() {
            return info;
        }
    }

    public Plan findBestPlanToServeRequest(HttpRequest request,
            Collection<RdfHttpEntityFile> entities,
            OpExecutor opExecutor) throws IOException {
        Header[] headers = request.getAllHeaders();

        List<MediaType> supportedContentTypes = HttpHeaderUtils.supportedMediaTypes();
        Collection<String> supportedEncodings = new ArrayList<>(Arrays.asList(IDENTITY_ENCODING));
        supportedEncodings.addAll(PathCoderRegistry.get().getCoderNames());


        // Get the requested content types in order of preference
        Map<MediaType, Float> requestedContentTypeRanges = HttpHeaderUtils.getOrderedValues(headers, HttpHeaders.ACCEPT).entrySet().stream()
                .collect(Collectors.toMap(e -> MediaType.parse(e.getKey()), Entry::getValue));

        // If no content type is requested then accept any
        if (requestedContentTypeRanges.isEmpty()) {
            for (RdfHttpEntity entity : entities) {
                RdfEntityInfo info = entity.getCombinedInfo().as(RdfEntityInfo.class);
                String mediaTypeStr = info.getContentType();
                if (mediaTypeStr != null) {
                    MediaType mediaType = MediaType.parse(mediaTypeStr);
                    requestedContentTypeRanges.put(mediaType, 1.0f);
                }
            }
        }


        // Get the requested encodings in order of preference
        Map<String, Float> requestedEncodings = HttpHeaderUtils.getOrderedValues(headers, HttpHeaders.ACCEPT_ENCODING);

        if(!requestedEncodings.containsKey(IDENTITY_ENCODING)) {
            requestedEncodings.put(IDENTITY_ENCODING, 1f);
        }


        // Filter the supported media types by the requested ones
        // The supported media type must match a range in the headers whose score is greater than 0
        Map<MediaType, Float> candidateTargetContentTypes = requestedContentTypeRanges.entrySet().stream()
                .filter(rangeEntry -> rangeEntry.getValue() > 0)
                .flatMap(rangeEntry -> supportedContentTypes.stream()
                        .filter(supported -> supported.is(rangeEntry.getKey()))
                        .map(supported -> Maps.immutableEntry(supported, rangeEntry.getValue())))
                .sorted((a, b) -> a.getValue().compareTo(b.getValue()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> a, LinkedHashMap::new));

        Map<String, Float> candidateEncodings = requestedEncodings.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .flatMap(entry -> supportedEncodings.stream()
                        .filter(supported -> supported.equalsIgnoreCase(entry.getKey()))
                        .map(supported -> Maps.immutableEntry(supported, entry.getValue())))
                .sorted((a, b) -> a.getValue().compareTo(b.getValue()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> a, LinkedHashMap::new));


        // TODO Abstract the cartesian product so we can extend to any number of dimensions
        //Table<RdfHttpEntityFile, Op, Float> entityToPlanToScore = HashBasedTable.create();
        Multimap<RdfHttpEntityFile, Entry<Plan, Float>> entityToPlan = HashMultimap.create();

        for(RdfHttpEntityFile entity : entities) {
            // TODO Ensure entities are valid
            // - e.g. manual deletion of files in the http cache can cause corruption

            RdfEntityInfo info = entity.getCombinedInfo().as(RdfEntityInfo.class);
            //MediaType mt = MediaType.parse(info.getContentType());

            for(Entry<MediaType, Float> e : candidateTargetContentTypes.entrySet()) {
                String tgtContentType = e.getKey().toString();
                Float tgtContentTypeScore = e.getValue();

                for(Entry<String, Float> f : candidateEncodings.entrySet()) {
                    String tgtEncoding = f.getKey();
                    Float tgtEncodingScore = f.getValue();

                    List<String> tgtEncodings = tgtEncoding.equalsIgnoreCase(IDENTITY_ENCODING)
                            ? Collections.emptyList()
                            : Arrays.asList(tgtEncoding);

                    Op op = Planner.createPlan(entity, tgtContentType, tgtEncodings);
                    if(op != null) {
                        op = opExecutor.optimizeInPlace(op);


                        int numOps = ResourceTreeUtils.getNumOps(op, Op::getChildren);

                        RdfEntityInfo meta = ModelFactory.createDefaultModel().createResource()
                                .as(RdfEntityInfo.class)
                                .setContentType(tgtContentType)
                                .setContentEncodings(tgtEncodings);

                        Plan plan = new Plan(op, meta);

                        Entry<Plan, Float> planAndScore = Maps.immutableEntry(plan, (float)numOps);

                        entityToPlan.put(entity, planAndScore);
                    }
                }
            }
        }

        Entry<RdfHttpEntityFile, Entry<Plan, Float>> entry = entityToPlan.entries().stream()
                .sorted((a, b) -> a.getValue().getValue().compareTo(b.getValue().getValue()))
                .findFirst()
                .orElse(null);

        Plan result = entry == null ? null : entry.getValue().getKey();

        return result;

        //Map<Path, Float> candidateToScore = new HashMap<>();


        // TODO Find best candidate among the file entities

//
//		// Pick entity with the best score
//		RdfHttpEntityFile entity = entityToScore.entrySet().stream()
//			.sorted((a, b) -> a.getValue().compareTo(b.getValue()))
//			.findFirst()
//			.map(Entry::getKey)
//			.orElse(null);

    }


    public static HttpRequest expandHttpRequest(HttpRequest request) {
        HttpUriRequest result =
                RequestBuilder
                .copy(request)
                .build();

        Header[] origHeaders = result.getAllHeaders();
        Header[] newHeaders = ContentTypeUtils.expandAccept(origHeaders);

        // TODO Add encoding

        result.setHeaders(newHeaders);

        return result;
    }

    public boolean validateEntity(RdfHttpEntityFile entity) {
        Path path = entity.getAbsolutePath();
        boolean result = Files.exists(path);

        return result;
    }

    /**
     * Lookup an entity
     *
     * First, this method checks if the request can be served from the locally cached entities:
     * It attempts to create a plan that transforms the available entities into a requested one.
     * If this fails, this method examines the cached resource vary headers for whether fetching a remote entity
     * can help to serve the request. If there is no cached resource,
     * a request to the remote server is made with expanded headers.
     *
     * If this leads to a new entity being generated, then the process of planning is repeated with it.
     *
     *
     * TODO I thought there was a way to enumerate in the HTTP headers for which values exists for Vary
     * By default Vary only says that a request with a different value for the given header name may yield a different representation
     *
     *
     * @param request
     * @param httpRequester
     * @return
     * @throws IOException
     */
    @Override
    public RdfHttpEntityFile get(HttpRequest request, Function<HttpRequest, Entry<HttpRequest, HttpResponse>> httpRequester) throws IOException {
        // Expand the request: Add compatible accept headers and encodings

        String uri = request.getRequestLine().getUri();


//		if(uri.contains("db3fc357b775f2e996f88c87ddfacc64/db3fc357b775f2e996f88c87ddfacc64.hdt")) {
//			System.out.println("DEBUG POINT");
//		}

        //RdfHttpResourceFile res = store.get(uri);


        Collection<RdfHttpEntityFile> entities = getEntities(uri);
        List<RdfHttpEntityFile> validatedEntities = entities.stream()
                .filter(this::validateEntity)
                .collect(Collectors.toList());

        OpExecutor opExecutor = new OpExecutor(this, hashStore);

        Plan plan = findBestPlanToServeRequest(request, validatedEntities, opExecutor);

        //result = null;
        if(plan == null) {
            RdfHttpResourceFile res = downloadStore.getResource(uri);
            HttpRequest newRequest = expandHttpRequest(request);

            if(httpRequester != null) {
                Entry<HttpRequest, HttpResponse> response = httpRequester.apply(newRequest);


                RdfHttpEntityFile entity = saveResponse(res, response.getKey(), response.getValue());


                // Validation step; the entity should match the
                plan = findBestPlanToServeRequest(request, Collections.singleton(entity), opExecutor);
            }
        }

        if(plan == null) {
            return null;
            //throw new RuntimeException("Could not create a plan for how to serve an HTTP request");
        }

//		// Convert the entity to the request
//		String bestEncoding = bestEncoding(encodings.keySet());
//		MediaType _bestContentType = bestContentType(mediaTypeRanges.keySet());
//
//		if(bestEncoding == null || _bestContentType == null) {
//			throw new RuntimeException("Could not find target content type / encoding: " + _bestContentType + " / " + bestEncoding);
//		}
//
//		String bestContentType = _bestContentType.toString();
//		Op op = Planner.createPlan(entity, bestContentType, Arrays.asList(bestEncoding));
//		RDFDataMgr.write(System.out, op.getModel(), RDFFormat.TURTLE_PRETTY);
//		System.out.println("Number of ops before optimization: " + OpUtils.getNumOps(op));
//
//		OpExecutor opExecutor = new OpExecutor(this, hashStore);
//
//		//ModelFactory.createDefaultModel()
//
//		op = opExecutor.optimizeInPlace(op);
//		System.out.println("Number of ops after optimization: " + OpUtils.getNumOps(op));

        Op op = plan.getOp();

        // TODO Output the plan to the logger
//		RDFDataMgr.write(System.out, op.getModel(), RDFFormat.TURTLE_PRETTY);


        Path tgt = op.accept(opExecutor);

        RdfHttpEntityFile entity;

        Path hashPath = hashStore.getAbsolutePath();
        // If the path points to the hash store, copy the result to the resources' cache
        if(tgt.startsWith(hashPath)) {
            if(!Files.isSymbolicLink(tgt)) {

                RdfEntityInfo meta = plan.getInfo();

                entity = cacheStore.allocateEntity(uri, meta);
                Path tgtPath = entity.getAbsolutePath();

                forceCreateDirectories(tgtPath.getParent());

                // HACK - Replace existing should not be needed
                try {
                    Files.move(tgt, tgtPath /*, StandardCopyOption.REPLACE_EXISTING */); /*, StandardCopyOption.ATOMIC_MOVE */
                }
                catch(Exception e) {
                    logger.warn("Should not happen: Failed move " + tgt + " to " + tgtPath, e);
                }
                // Note: It is important that we relativize based on the target file's directory,
                // hence tgt.getParent()
                Path relTgtPath = tgt.getParent().relativize(tgtPath);

                Files.createSymbolicLink(tgt, relTgtPath);

                entity = cacheStore.getEntityForPath(tgtPath);

                computeHashForEntity(entity, null);
            } else {
                Path relPathTgt = Files.readSymbolicLink(tgt);
                Path absPath = tgt.getParent().resolve(relPathTgt).normalize();
                entity = getEntityForPath(absPath);
            }

        } else {
            entity = getEntityForPath(tgt);
        }



        return entity;
    }


    public static void forceCreateFile(Path path) {
        try {
            Files.createFile(path);
        } catch (FileAlreadyExistsException e) {
            // Ignored
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void forceCreateDirectories(Path path) {
        try {
            Files.createDirectories(path);
        } catch (FileAlreadyExistsException e) {
            // Ignored
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Compute a hash (sha256) for the content at path tmp and associate it with the given
     * entity. If tmp is null, use the content of the entity.
     *
     * @param rdfEntity
     * @param tmp
     */
    public static void computeHashForEntity(RdfHttpEntityFile rdfEntity, Path tmp) {
        Path targetPath = rdfEntity.getAbsolutePath();

        if(tmp == null) {
            tmp = targetPath;
        }


        // Compute hash
        ByteSource bs = org.apache.jena.ext.com.google.common.io.Files.asByteSource(tmp.toFile());

        HashCode hashCode;
        try {
            hashCode = bs.hash(Hashing.sha256());
            String str = hashCode.toString();

            forceCreateFile(targetPath);

            rdfEntity.updateInfo(info -> {
                Checksum hi = info.getModel().createResource().as(Checksum.class);

                hi.setAlgorithm("sha256").setChecksum(str);
                Collection<Checksum> hashes = info.as(RdfEntityInfo.class).getHashes();
                hashes.add(hi);
            });

        } catch(Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Derives the suffix which to append to the base path from the entity's headers.
     *
     * @param basePath
     * @param entity
     * @throws IOException
     * @throws UnsupportedOperationException
     */
    public RdfHttpEntityFile saveResponse(RdfHttpResourceFile targetResource, HttpRequest request, HttpResponse response) throws UnsupportedOperationException, IOException {
        HttpEntity entity = response.getEntity();

        // If the type is application/octet-steam we
        // can try to derive content type and encodings from
        // a content-disposition header or the original URI
        // In fact, we can try both things, and see whether any yields results
        // the results can be verified afterwards (e.g. by Files.probeContentType)
        // hm, since content-disposition seems to be non-standard maybe we can also just ignore it

        String ct =  HttpHeaderUtils.getValueOrNull(entity.getContentType());

        // TODO Move the logic to derive the headers we want elsewhere
        // E.g. apache2 may return gzip files as content type instead of encoding
        RdfEntityInfo meta = HttpHeaderUtils.copyMetaData(entity, null);
        String uri = request.getRequestLine().getUri();
        if(ct == null
                || ct.equalsIgnoreCase(ContentType.APPLICATION_OCTET_STREAM.getMimeType())
                || ct.equalsIgnoreCase(ContentType.TEXT_PLAIN.getMimeType())
                || ct.equalsIgnoreCase(ContentType.parse("application/x-gzip").getMimeType())
//				|| ct.equalsIgnoreCase(ContentType.parse("application/x-bzip").getMimeType())
                || ct.equalsIgnoreCase(ContentType.parse("application/x-bzip2").getMimeType())
                ) {
            meta = ContentTypeUtils.deriveHeadersFromFileExtension(uri);
        }

        RdfHttpEntityFile rdfEntity = targetResource.allocate(meta);

        //Path targetPath = res.getPath().resolve("data");
        Path targetPath = rdfEntity.getAbsolutePath();

        // HACK - this assumes the target path refers to a file (and not a directory)!
        Files.createDirectories(targetPath.getParent());

        Path tmp = FileUtils.allocateTmpFile(targetPath);
        Files.copy(entity.getContent(), tmp, StandardCopyOption.REPLACE_EXISTING);

        // Issue: computeHashForEntity requires the entity file to exist and thus creates a zero byte file
        // However, Files.move will then cause a file already exists exception
        computeHashForEntity(rdfEntity, tmp);

        logger.info("For url " + uri + " moving file " + tmp + " to " + targetPath);
        Files.move(tmp, targetPath, StandardCopyOption.REPLACE_EXISTING /*, StandardCopyOption.ATOMIC_MOVE */);

        //RdfFileEntity result = new RdfFileEntityImpl(finalPath, meta);
//		result.setContentType(meta.getContentType());
//		result.setContentEncoding(meta.getContentEncoding());

        return rdfEntity;
    }

    /**
     * May rewrite an original request and returns it together with its response
     *
     * @param request
     * @return
     */
    public static Entry<HttpRequest, HttpResponse> resolveRequest(HttpRequest request) {
        String url = request.getRequestLine().getUri();
//
//		// Extract a dataset id from the URI
//		// Check all data catalogs for whether they can resolve the id
//
//		// Fake a request to a catalog for now - the result is a dcat model
//		Model m = RDFDataMgr.loadModel("/home/raven/.dcat/repository/datasets/data/www.example.org/dataset-dbpedia-2016-10-core/_content/dcat.ttl");
//
//		//System.out.println(m.size());
//
//		String url = "http://downloads.dbpedia.org/2016-10/core-i18n/en/genders_en.ttl.bz2";
//		//String url = m.listObjectsOfProperty(DCAT.downloadURL).mapWith(x -> x.asNode().getURI()).next();
//		System.out.println(url);

        HttpClient client = HttpClientBuilder.create().build();


        HttpUriRequest myRequest =
                RequestBuilder
                .copy(request)
                .setUri(url)
                .build();

        //new DefaultHttpRequestFactory().
        HttpResponse response;
        try {
            response = client.execute(myRequest);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        //client.execute(request, context)

        //m.listObjectsOfProperty(DCAT.downloadURL).toList();

        //throw new RuntimeException("not implemented yet");
        return Maps.immutableEntry(myRequest, response);
    }

    public static Path getDefaultPath() {
        String homeDir = StandardSystemProperty.USER_HOME.value();
        Path result = Paths.get(homeDir).resolve(".dcat/repository");

        return result;
    }
    public static HttpResourceRepositoryFromFileSystemImpl createDefault() throws IOException {
        Path root = getDefaultPath();
        Files.createDirectories(root);

        HttpResourceRepositoryFromFileSystemImpl result = create(root);

        return result;
    }

    public static void main(String[] args) throws IOException {
        JenaSystem.init();


        Header[] expansionTest = new Header[] { new BasicHeader(HttpHeaders.ACCEPT, WebContent.contentTypeTurtleAlt1 + ";q=0.3")};
//		Header[] expansionTest = new Header[] { new BasicHeader(HttpHeaders.ACCEPT, WebContent.contentTypeTurtleAlt2 + ",text/plain;q=0.5")};
        expansionTest = ContentTypeUtils.expandAccept(expansionTest);
        System.out.println("Expanded: " + Arrays.asList(expansionTest));

//		if(true) {
//			return;
//		}

        Path root = Paths.get("/home/raven/.dcat/test3");
        Files.createDirectories(root);

        HttpResourceRepositoryFromFileSystemImpl manager = create(root);

        ResourceStore store = manager.getDownloadStore();
        ResourceStore hashStore = manager.getHashStore();


//		String url = "/home/raven/.dcat/test3/genders_en.ttl.bz2";
        String url = "http://downloads.dbpedia.org/2016-10/core-i18n/en/genders_en.ttl.bz2";
//		String url = "/home/raven/Projects/limbo/git/train_3-dataset/target/metadata-catalog/catalog.all.ttl";
//		Model m = RDFDataMgr.loadModel(url);
//		try(RDFConnection conn = RDFConnectionFactory.connect(DatasetFactory.wrap(m))) {
//			for(int i = 0; i < 10; ++i) {
//				try(QueryExecution qe = conn.query("SELECT * { ?s ?p ?o BIND(RAND() AS ?sortKey) } ORDER BY ?sortKey LIMIT 1")) {
//					System.out.println(ResultSetFormatter.asText(qe.execSelect()));
//				}
//			}
//		}

        RdfHttpEntityFile entity = HttpResourceRepositoryFromFileSystemImpl
                .get(manager, url, WebContent.contentTypeRDFXML, Arrays.asList("bzip2"));
        //RdfHttpEntityFile entity = manager.get(url, WebContent.contentTypeTurtle, Arrays.asList("gzip"));

        //RdfHttpResourceFile res = store.getResource(url);
        //RdfHttpEntityFile entity = res.getEntities().iterator().next();

        //Planner.execute(op);


        if(true) {
            return;
        }

//		RdfFileResource res = rm.get("http://downloads.dbpedia.org/2016-10/core-i18n/en/genders_en.ttl.bz2");



        BasicHttpRequest r = new BasicHttpRequest("GET", url);
        r.setHeader(HttpHeaders.ACCEPT, WebContent.contentTypeTurtleAlt1);
        r.setHeader(HttpHeaders.ACCEPT_ENCODING, "gzip,identity;q=0");

        manager.get(r, HttpResourceRepositoryFromFileSystemImpl::resolveRequest);

    }
}
