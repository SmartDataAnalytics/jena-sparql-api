package org.aksw.jena_sparql_api.http.repository.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.io.util.UriToPathUtils;
import org.aksw.jena_sparql_api.conjure.utils.ContentTypeUtils;
import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;
import org.aksw.jena_sparql_api.http.repository.api.HttpResourceRepositoryFromFileSystem;
import org.aksw.jena_sparql_api.http.repository.api.PathAnnotatorRdf;
import org.aksw.jena_sparql_api.http.repository.api.RdfHttpEntityFile;
import org.aksw.jena_sparql_api.http.repository.api.RdfHttpResourceFile;
import org.aksw.jena_sparql_api.http.repository.api.ResourceStore;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



interface ResourceSourceFile {
    Resource getResource(Path path);
}


class ResourceSourceFileImpl
    implements ResourceSourceFile
{
    @Override
    public Resource getResource(Path path) {
        String fileName = path.getFileName().toString();
        RdfEntityInfo result = ContentTypeUtils.deriveHeadersFromFileExtension(fileName);
        return result;
    }
}

class ResourceUtils {

    /**
     * Copies all direct outgoing properties of src to tgt
     *
     * @param tgt
     * @param src
     * @return
     */
    public static Resource copyDirectProperties(Resource tgt, Resource src) {
        StmtIterator it = src.listProperties();
        while(it.hasNext()) {
            Statement stmt = it.next();

            //RDFNode s = stmt.getSubject();
            Property p = stmt.getPredicate();
            RDFNode o = stmt.getObject();

            tgt.addProperty(p, o);
        }

        return tgt;
    }


    public static Resource merge(Collection<Resource> resources) {
        Model m = ModelFactory.createDefaultModel();
        Resource result = m.createResource();

        for(Resource r : resources) {
            Objects.requireNonNull(r);
            Model n = r.getModel();
            StmtIterator it = n.listStatements();
            while(it.hasNext()) {
                Statement stmt = it.next();

                RDFNode s = stmt.getSubject();
                Property p = stmt.getPredicate();
                RDFNode o = stmt.getObject();

                if(s.equals(r)) { s = result; }
                if(o.equals(r)) { o = result; }

                // Should never be not a resource here
//				if(s.isResource()) {
                m.add(s.asResource(), p, o);
//				}
            }
        }

        return result;
    }
}


/**
 * ResourceManager for a single folder; does not manage multiple repositories
 *
 * @author raven
 *
 */
public class ResourceStoreImpl
    implements ResourceStore
{
    protected String TMP_SUFFIX = ".tmp";


    protected Path basePath;

    protected String CONTENT = "_content";

    protected ResourceSourceFile resourceSource;
    protected PathAnnotatorRdf pathAnnotator;
    protected Function<String, Path> uriToRelPath;

    private static final Logger logger = LoggerFactory.getLogger(ResourceStore.class);

    /**
     * Find a model in the given resource repository or create one it in the given store based on
     * the lambda if it does not yet exist
     *
     * @param repo
     * @param store
     * @param uri
     * @param preferredOutputFormat
     * @param modelSupplier
     * @return
     * @throws IOException
     */
    public static Entry<RdfHttpEntityFile, Model> requestModel(HttpResourceRepositoryFromFileSystem repo, ResourceStore store, String uri, RDFFormat preferredOutputFormat, Supplier<Model> modelSupplier) throws IOException {
        Entry<RdfHttpEntityFile, Model> result = getOrCacheEntity(repo, store, uri, new HttpObjectSerializerModel(preferredOutputFormat), modelSupplier);

        return result;
    }



    /**
     * Request an RDF model from the repository based on a given uri (or any string)
     *
     * @param repo
     * @param store
     * @param uri
     * @param preferredOutputformat
     * @param modelSupplier
     * @return
     * @throws IOException
     */
    public static <T> Entry<RdfHttpEntityFile, T> getOrCacheEntity(
            HttpResourceRepositoryFromFileSystem repo,
            ResourceStore store,
            String uri,
            HttpObjectSerializer<T> serializer,
            Supplier<T> contentSupplier) throws IOException {

        RdfHttpEntityFile entity;
        T content;

        HttpUriRequest baseRequest = serializer.createHttpRequest(uri);
//				RequestBuilder.get(uri)
//				.setHeader(HttpHeaders.ACCEPT, "application/n-triples")
//				.setHeader(HttpHeaders.ACCEPT_ENCODING, "identity,bzip2,gzip")
//				.build();

        HttpRequest effectiveRequest = HttpResourceRepositoryFromFileSystemImpl.expandHttpRequest(baseRequest);
        logger.info("Expanded HTTP Request: " + effectiveRequest);
        try {
            entity = repo.get(effectiveRequest, null);
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }

        if(entity != null) {
            logger.info("Serving " + uri + " from cache");
            content = serializer.deserialize(entity);
//			String absPath = entity.getAbsolutePath().toString();
//			model = RDFDataMgr.loadModel(absPath);
        } else {
            logger.info("Serving" + uri + " from computation and adding to cache");

            content = contentSupplier.get();
            entity = serializer.serialize(uri, store, content);

            boolean sanityCheck = true;
            // Check whether the written data is readable
            // Corruption may be caused e.g. by race condiditons
            if(sanityCheck) {
                String file = entity.getAbsolutePath().toString();
                Model sanityCheckModel = RDFDataMgr.loadModel(file);
                logger.info("Sanity check result: Model has " + sanityCheckModel.size() + " triples");
            }

            //RDFLanguages.fi
//			RDFFormat effectiveOutFormat;
//			String fileExt = Iterables.getFirst(preferredOutFormat.getLang().getFileExtensions(), null);
//			effectiveOutFormat = fileExt == null
//					? RDFFormat.TURTLE_PRETTY
//					: preferredOutFormat;
//
//			fileExt = Iterables.getFirst(effectiveOutFormat.getLang().getFileExtensions(), null);
//
//			Objects.requireNonNull(fileExt, "Should not happen");
//
//			model = modelSupplier.get();
//			java.nio.file.Path tmpFile = Files.createTempFile("data-", fileExt);
//			try(OutputStream out = Files.newOutputStream(tmpFile, StandardOpenOption.WRITE)) {
//				RDFDataMgr.write(out, model, effectiveOutFormat);
//			} catch (IOException e1) {
//				throw new RuntimeException(e1);
//			}
//
//			RdfEntityInfo entityInfo = ModelFactory.createDefaultModel().createResource().as(RdfEntityInfo.class)
//					.setContentType(effectiveOutFormat.getLang().getContentType().getContentType());
//			entity = store.putWithMove(uri, entityInfo, tmpFile);
//			HttpResourceRepositoryFromFileSystemImpl.computeHashForEntity(entity, null);
        }

        return Maps.immutableEntry(entity, content);
    }

    public ResourceStoreImpl(Path basePath) {
        this(basePath, UriToPathUtils::resolvePath);
    }

    public ResourceStoreImpl(Path basePath, Function<String, Path> uriToRelPath) {
        super();
        this.basePath = basePath;
        this.uriToRelPath = uriToRelPath;

        this.pathAnnotator = new PathAnnotatorRdfImpl();
        this.resourceSource = new ResourceSourceFileImpl();
    }


    @Override
    public Path getAbsolutePath() {
        return basePath;
    }

    public Resource getInfo(Path absPath) {
        Resource r1 = resourceSource.getResource(absPath);
        Resource r2 = pathAnnotator.getRecord(absPath);

        Resource result = ResourceUtils.merge(Arrays.asList(r1, r2));

        return result;
    }

    public Path fullPath(String uri) {
        Path relPath = uriToRelPath.apply(uri);
        Path result = basePath.resolve(relPath);

        return result;
    }

    public RdfHttpResourceFile get(String uri) {
        Path fullPath = fullPath(uri);
        RdfHttpResourceFileImpl result = new RdfHttpResourceFileImpl(this, fullPath);
        return result;
    }

    /**
     * Put a given file into an appropriate place in the repository
     * using a move operation
     *
     * @param uri
     * @param file
     * @return
     * @throws IOException
     */
    public RdfHttpEntityFile putWithMove(String uri, RdfEntityInfo metadata, Path file) throws IOException {
        RdfHttpEntityFile result = allocateEntity(uri, metadata);
        Path tgtFile = result.getAbsolutePath();
        Files.createDirectories(tgtFile.getParent());
        Files.move(file, tgtFile /*, StandardCopyOption.ATOMIC_MOVE */, StandardCopyOption.REPLACE_EXISTING);
        result.updateInfo(record -> ResourceUtils.copyDirectProperties(record, metadata));

        return result;
    }



    public RdfHttpEntityFile getEntityForPath(Path absEntityPath) {
        RdfHttpEntityFile result;

        if(absEntityPath.startsWith(basePath)) {

            String fileName = absEntityPath.getFileName().toString();

            Path parent = absEntityPath.getParent();
            String parentFileName = parent.getFileName().toString();

            if(parentFileName.equals(CONTENT)) {
                Path resRelFolder = basePath.relativize(parent);

                Path entityRelFolder = parent.relativize(absEntityPath);
                //Path resFolder = parent.getParent();
                RdfHttpResourceFileImpl res = new RdfHttpResourceFileImpl(this, resRelFolder);
                result = new RdfHttpEntityFileImpl(res, entityRelFolder);
            } else {
                result = null;
            }
        } else {
            result = null;
        }
        //Resource cachedInfo = pathAnnotator.getRecord(path);

//		RdfEntityInfo info = ContentTypeUtils.deriveHeadersFromFileExtension(fileName);

        return result;
    }


//	EntitySpace getEntitySpace(Path basePath) {
//
//	}

    public RdfHttpResourceFile getResource(String uri) {
        Path path = uriToRelPath.apply(uri);

        path = path.resolve(CONTENT);

        RdfHttpResourceFile result = new RdfHttpResourceFileImpl(this, path);
        return result;
    }

    public Collection<RdfHttpEntityFile> listEntities(Path relContentFolder) {
        //Path contentFolder = basePath.resolve(CONTENT);

        Path contentFolder = basePath.resolve(relContentFolder);

        List<RdfHttpEntityFile> result;

        try {
            if(!Files.exists(contentFolder)) {
                result = Collections.emptyList();
            } else {
                try(Stream<Path> stream = Files.list(contentFolder)) {
                    result = stream.filter(file -> pathAnnotator.isAnnotationFor(file).isEmpty())
                            // skip .tmp files
                            .filter(file -> !file.getFileName().toString().endsWith(TMP_SUFFIX))
                            .map(this::getEntityForPath)
                            .collect(Collectors.toList());

                }
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    @Override
    public boolean contains(Path path) {
        path = path.toAbsolutePath();

        boolean result = path.startsWith(basePath);
        return result;
    }

    @Override
    public Resource getInfo(Path path, String layer) {
        Resource result = contains(path)
            ? pathAnnotator.getRecord(path)
            : null;

        return result;
    }

    @Override
    public RdfHttpEntityFile allocateEntity(String uri, Resource description) {
//		Path relPath = uriToRelPath.apply(uri);
//		path = path.resolve(CONTENT);

        RdfHttpResourceFile res = getResource(uri);
        Path resPath = res.getAbsolutePath();

        RdfHttpEntityFile result = allocateEntity(resPath, description);
        return result;
    }

    public RdfHttpResourceFile pathToResource(Path baseRelPath) {
        // TODO Validate the path
        RdfHttpResourceFile result = new RdfHttpResourceFileImpl(this, baseRelPath);

        return result;
    }

    public RdfHttpEntityFile allocateEntity(Path baseRelPath, Resource _info) {

        RdfEntityInfo info = _info.as(RdfEntityInfo.class);

        String suffix = ContentTypeUtils.toFileExtension(info);
        //pathToResource(baseRelPath);
        Path finalRelPath = Paths.get("data" + suffix);


        RdfHttpResourceFile res = pathToResource(baseRelPath);


        RdfHttpEntityFile result = new RdfHttpEntityFileImpl(res, finalRelPath);

        return result;
    }


    @Override
    public void updateInfo(Path path, Consumer<? super Resource> callback) {
        Resource r = pathAnnotator.getRecord(path);
        if(r == null) {
            throw new RuntimeException("Cannot update record of non-existent content file");
        }

        callback.accept(r);
        pathAnnotator.setRecord(path, r);
    }

}
