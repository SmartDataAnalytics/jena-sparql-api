package org.aksw.jena_sparql_api.rx;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import org.aksw.jena_sparql_api.rx.entity.EntityInfo;
import org.aksw.jena_sparql_api.rx.entity.EntityInfoImpl;
import org.aksw.jena_sparql_api.util.iri.IRIxResolverUtils;
import org.aksw.jena_sparql_api.utils.io.StreamRDFWriterEx;
import org.aksw.jena_sparql_api.utils.io.WriterStreamRDFBaseUtils;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.ext.com.google.common.collect.ArrayListMultimap;
import org.apache.jena.ext.com.google.common.collect.Multimap;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.irix.IRIx;
import org.apache.jena.irix.IRIxResolver;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.riot.RIOT;
import org.apache.jena.riot.out.NodeToLabel;
import org.apache.jena.riot.resultset.ResultSetReaderRegistry;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFOps;
import org.apache.jena.riot.writer.WriterStreamRDFBase;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sys.JenaSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.rxjava3.core.Flowable;

/**
 * Extensions to help open an InputStream of unknown content using probing against languages registered to the Jena riot system.
 * This includes languages based on triples, quads and result sets. Support for further types may be added in the future.
 *
 * @author Claus Stadler, Dec 18, 2018
 *
 */
public class RDFDataMgrEx {
    private static final Logger logger = LoggerFactory.getLogger(RDFDataMgrEx.class);

    static { JenaSystem.init(); }

    public static final List<Lang> DEFAULT_PROBE_LANGS = Collections.unmodifiableList(Arrays.asList(
            RDFLanguages.TRIG, // Subsumes turtle, nquads and ntriples
            RDFLanguages.JSONLD,
            RDFLanguages.RDFXML,
            RDFLanguages.RDFTHRIFT
            // RDFLanguages.TRIX
    ));

    public static boolean isStdIn(String filenameOrIri) {
        return "-".equals(filenameOrIri);
    }

    /**
     * Map a TypedInputStream's media type to a Lang
     *
     * @param tin
     * @return
     */
    public static Lang getLang(TypedInputStream tin) {
        ContentType ct = tin.getMediaType();
        Lang result = RDFLanguages.contentTypeToLang(ct);
        return result;
    }

    public static void read(Model model, TypedInputStream tin) {
        Lang lang = getLang(tin);
        RDFParser.create()
            .forceLang(lang)
            .source(tin.getInputStream())
            .base(tin.getBaseURI())
            .parse(model);
    }

    /**
     * Return a TypedInputStream whose underlying InputStream supports marks
     * If the original one already supports it it is returned as is.
     *
     * @param tin
     * @return
     */
    public static TypedInputStream forceBuffered(TypedInputStream tin) {
        TypedInputStream result = tin.markSupported()
                ? tin
                : wrapInputStream(new BufferedInputStream(tin.getInputStream()), tin);

        return result;
    }

    public static InputStream forceBuffered(InputStream in) {
        InputStream result = in.markSupported()
                ? in
                : new BufferedInputStream(in);

        return result;
    }

    /**
     * Wrap an InputStream as a TypedInputStream based on the attributes of the latter
     *
     * @param in
     * @param proto
     * @return
     */
    public static TypedInputStream wrapInputStream(InputStream in, TypedInputStream proto) {
        TypedInputStream result = new TypedInputStream(in, proto.getMediaType(), proto.getBaseURI());

        return result;
    }


    /**
     * Decode a given input stream based on a sequence of codec names.
     *
     * @param in
     * @param codecs
     * @param csf
     * @return
     * @throws CompressorException
     */
    public static InputStream decode(InputStream in, List<String> codecs, CompressorStreamFactory csf)
            throws CompressorException {
        InputStream result = in;
        for (String encoding : codecs) {
            result = csf.createCompressorInputStream(encoding, result, true);
        }
        return result;
    }

    /**
     * Probe an input stream for any encodings (e.g. using compression codecs) and
     * its eventual content type.
     *
     * <pre>
     * try (InputStream in = ...) {
     *   EntityInfo entityInfo = probeEntityInfo(in, RDFDataMgrEx.DEFAULT_PROBE_LANGS);
     * }
     * </pre>
     *
     * @param in
     * @param candidates
     * @return
     * @throws IOException
     */
    public static EntityInfo probeEntityInfo(InputStream in, Iterable<Lang> candidates) throws IOException {
        if (!in.markSupported()) {
            in = new BufferedInputStream(in);
        }
        in.mark(1024 * 1024 * 1024);

        CompressorStreamFactory csf = CompressorStreamFactory.getSingleton();

        EntityInfo result;
        try (InputStream is = in) {

            InputStream nextIn = is;
            List<String> encodings = new ArrayList<>();
            for (;;) {
                String encoding;
                try {
                    encoding = CompressorStreamFactory.detect(is);
                } catch (CompressorException e) {
                    break;
                } finally {
                    is.reset();
                }
                encodings.add(encoding);

                try {
                    nextIn = new BufferedInputStream(decode(is, encodings, csf));
                } catch (CompressorException e) {
                    // Should not fail here because we applied detect() before
                    throw new RuntimeException(e);
                }
            }

            try (TypedInputStream tis = RDFDataMgrEx.probeLang(nextIn, candidates)) {
                String contentType = tis.getContentType();
                String charset = tis.getCharset();
                result = new EntityInfoImpl(encodings, contentType, charset);
            }
        }

        return result;
    }

    public static TypedInputStream probeLang(InputStream in, Iterable<Lang> candidates) {
        return probeLang(in, candidates, true);
    }


    /**
     * Probe the content of the input stream against a given set of candidate languages.
     * Wraps the input stream as a BufferedInputStream and can thus also probe on STDIN.
     * This is also the reason why the method does not take an InputStream supplier as argument.
     *
     * The result is a TypedInputStream which combines the BufferedInputStream with content
     * type information
     *
     *
     * @param in
     * @param candidates
     * @param tryAllCandidates If true do not accept the first successful candidate; instead try all candidates and pick the one that yields most data
     *
     * @return
     */
    public static TypedInputStream probeLang(
            InputStream in,
            Iterable<Lang> candidates,
            boolean tryAllCandidates) {
        if (!in.markSupported()) {
            throw new IllegalArgumentException("Language probing requires an input stream with mark support");
        }

//        BufferedInputStream bin = new BufferedInputStream(in);

        // Here we rely on the VM/JDK not allocating the buffer right away but only
        // using this as the max buffer size
        // 1GB should be safe enough even for cases with huge literals such as for
        // large spatial geometries (I encountered some around ~50MB)
        in.mark(1 * 1024 * 1024 * 1024);

        Multimap<Long, Lang> successCountToLang = ArrayListMultimap.create();
        for(Lang cand : candidates) {
            @SuppressWarnings("resource")
            CloseShieldInputStream wbin = new CloseShieldInputStream(in);

            //bin.mark(Integer.MAX_VALUE >> 1);
            Flowable<?> flow;
            if (RDFLanguages.isQuads(cand)) {
                flow = RDFDataMgrRx.createFlowableQuads(() -> wbin, cand, null);
            } else if (RDFLanguages.isTriples(cand)) {
                flow = RDFDataMgrRx.createFlowableTriples(() -> wbin, cand, null);
            } else if (ResultSetReaderRegistry.isRegistered(cand)) {
                flow = RDFDataMgrRx.createFlowableBindings(() -> wbin, cand);
            } else {
                logger.warn("Skipping probing of unknown Lang: " + cand);
                continue;
            }

            // Stopwatch sw = Stopwatch.createStarted();

            // TODO If there is a syntax error within the first n items
            // then the format won't be recognized at all
            // We should add an indirection layer that allows to configure the prober
            // and query its result before allowing the client to obtain the input stream
            int n = 100;
            try {
                long count = flow.take(n)
                        .count()
                        .blockingGet();

                successCountToLang.put(count, cand);

                logger.debug("Number of items parsed by content type probing for " + cand + ": " + count);
            } catch(Exception e) {
                logger.debug("Failed to probe with format " + cand, e);
                continue;
            } finally {
                // logger.debug("Probing format " + cand + " took " + sw.elapsed(TimeUnit.MILLISECONDS));

                try {
                    in.reset();
                } catch (IOException x) {
                    throw new RuntimeException(x);
                }
            }

            if (!tryAllCandidates) {
                break;
            }
        }

        Entry<Long, Lang> bestCand = successCountToLang.entries().stream()
            .sorted((a, b) -> b.getKey().compareTo(a.getKey()))
            .findFirst()
            .orElse(null);

        ContentType bestContentType = bestCand == null ? null : bestCand.getValue().getContentType();
        TypedInputStream result = new TypedInputStream(in, bestContentType);

        return result;
    }


    public static void peek(InputStream in) {
        in.mark(1 * 1024 * 1024 * 1024);

        try {
            System.err.println("GOT:");
            System.err.println(IOUtils.toString(in));
            System.err.println("DONE");
            in.reset();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Attempts to open the given src and probe for the content type
     * Src may be '-' but not NULL in order to refer to STDIN.
     *
     * @param src
     * @param probeLangs
     * @return
     */
    public static TypedInputStream open(String src, Iterable<Lang> probeLangs) {
        Objects.requireNonNull(src);

        boolean useStdIn = isStdIn(src);

        TypedInputStream result;
        if(useStdIn) {
            // Use the close shield to prevent closing stdin on .close()
            // TODO Investigate if this is redundant; RDFDataMgr might already do it

            // FIXME Does not work for encoded streams; for those we would have to go through
            // Jena's StreamManager
            result = probeLang(new BufferedInputStream(System.in), probeLangs);
        } else {
            result = Objects.requireNonNull(RDFDataMgr.open(src), "Could not create input stream from " + src);

            // TODO Should we rely on the content type returned by RDFDataMgr? It may be based on e.g. a file extension
            // rather than the actual content - so we may be fooled here
            ContentType mediaType = result.getMediaType();
            if (mediaType != null) {
                // Check if the detected content type matches the ones we are probing for
                // If not then unset the content type and probe the content again
                String mediaTypeStr = mediaType.toHeaderString();
                boolean mediaTypeInProbeLangs = Streams.stream(probeLangs)
                        .anyMatch(lang -> RDFLanguagesEx.getAllContentTypes(lang).contains(mediaTypeStr));

                if (!mediaTypeInProbeLangs) {
                    mediaType = null;
                }
            }

            if(mediaType == null) {
                result = probeLang(forceBuffered(result.getInputStream()), probeLangs);
            }

        }

        return result;
    }


    public static RDFIterator<Triple> createIteratorTriples(PrefixMapping prefixMapping, InputStream in, Lang lang) {
        InputStream combined = prependWithPrefixes(in, prefixMapping);
        RDFIterator<Triple> it = RDFDataMgrRx.createIteratorTriples(combined, lang, null, (thread, throwable) -> {}, thread -> {});
        return it;
    }


    public static RDFIterator<Quad> createIteratorQuads(PrefixMapping prefixMapping, InputStream in, Lang lang) {
        InputStream combined = prependWithPrefixes(in, prefixMapping);
        RDFIterator<Quad> it = RDFDataMgrRx.createIteratorQuads(combined, lang, null, (thread, throwable) -> {}, thread -> {});
        return it;
    }

    public static Dataset parseTrigAgainstDataset(Dataset dataset, PrefixMapping prefixMapping, InputStream in) {
        // Add namespaces from the spec
        // Apparently Jena does not support parsing against
        // namespace prefixes previously declared in the target model
        // Therefore we serialize the prefix declarations and prepend them to the
        // input stream of the dataset
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		Model tmp = ModelFactory.createDefaultModel();
//		tmp.setNsPrefixes(prefixMapping);
//		RDFDataMgr.write(baos, tmp, Lang.TURTLE);
////		System.out.println("Prefix str: " + baos.toString());
//
//		InputStream combined = new SequenceInputStream(
//				new ByteArrayInputStream(baos.toByteArray()), in);
//
        InputStream combined = prependWithPrefixes(in, prefixMapping);
        RDFDataMgr.read(dataset, combined, Lang.TRIG);

        return dataset;
    }


    /**
     * Parse the input stream as turtle, thereby prepending a serialization of the given prefix mapping.
     * This is a workaround for Jena's riot framework - especially RDFParser - apparently not supporting
     * injecting a prefix mapping.
     *
     *
     * @param model
     * @param prefixMapping
     * @param in
     * @return
     */
    public static Model parseTurtleAgainstModel(Model model, PrefixMapping prefixMapping, InputStream in) {
        // Add namespaces from the spec
        // Apparently Jena does not support parsing against
        // namespace prefixes previously declared in the target model
        // Therefore we serialize the prefix declarations and prepend them to the
        // input stream of the dataset
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Model tmp = ModelFactory.createDefaultModel();
        tmp.setNsPrefixes(prefixMapping);
        RDFDataMgr.write(baos, tmp, Lang.TURTLE);

        InputStream combined = new SequenceInputStream(
                new ByteArrayInputStream(baos.toByteArray()), in);

        RDFDataMgr.read(model, combined, Lang.TURTLE);

        return model;
    }

    /**
     * Convenience method to prepend prefixes to an input stream (in turtle syntax)
     *
     * @param in
     * @param prefixMapping
     * @return
     */
    public static InputStream prependWithPrefixes(InputStream in, PrefixMapping prefixMapping) {
         return prependWithPrefixes(in, prefixMapping, RDFFormat.TURTLE_PRETTY);
    }

    /**
     * Convenience method to prepend prefixes to an input stream (in a given format)
     *
     * @param in
     * @param prefixMapping
     * @return
     */
    public static InputStream prependWithPrefixes(InputStream in, PrefixMapping prefixMapping, RDFFormat fmt) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Model tmp = ModelFactory.createDefaultModel();
        tmp.setNsPrefixes(prefixMapping);
        RDFDataMgr.write(baos, tmp, fmt);
//		System.out.println("Prefix str: " + baos.toString());

        InputStream combined = new SequenceInputStream(
                new ByteArrayInputStream(baos.toByteArray()), in);

        return combined;
    }


    public static TypedInputStream prependWithPrefixes(TypedInputStream in, PrefixMapping prefixMapping) {
        InputStream combined = prependWithPrefixes(in.getInputStream(), prefixMapping);

        TypedInputStream result = new TypedInputStream(combined, in.getMediaType(), in.getBaseURI());
        return result;
    }

    /** Return a preconfigured parser builder that retains blank node ids and relative IRIs */
    public static RDFParserBuilder newParserBuilderForReadAsGiven(String baseIri) {
        IRIxResolver resolver = IRIxResolverUtils.newIRIxResolverAsGiven(baseIri);

        return RDFParser.create()
            .resolver(resolver)
            .context(null)
            .base(null)
            .errorHandler(RDFDataMgrRx.dftErrorHandler())
            .labelToNode(RDFDataMgrRx.createLabelToNodeAsGivenOrRandom());
    }


    public static void readAsGiven(Graph graph, String uri) {
        newParserBuilderForReadAsGiven(null).source(uri).parse(graph);
    }

    public static void readAsGiven(Model model, String uri) {
        readAsGiven(model.getGraph(), uri);
    }

    public static Model loadModelAsGiven(String uri) {
        Model result = ModelFactoryEx.createInsertOrderPreservingModel();
        readAsGiven(result, uri);
        return result;
    }

    public static void readAsGiven(DatasetGraph datasetGraph, String uri, String baseIri) {
        newParserBuilderForReadAsGiven(baseIri).source(uri).parse(datasetGraph);
    }

    public static void readAsGiven(Dataset dataset, String uri, String baseIri) {
        readAsGiven(dataset.asDatasetGraph(), uri, baseIri);
    }

    public static void readAsGiven(DatasetGraph datasetGraph, InputStream in, Lang lang) {
        newParserBuilderForReadAsGiven(null).source(in).lang(lang).build().parse(datasetGraph);
    }

    public static void readAsGiven(Dataset dataset, InputStream in, Lang lang) {
        readAsGiven(dataset.asDatasetGraph(), in, lang);
    }

    public static Dataset loadDatasetAsGiven(String uri, String baseIri) {
        Dataset result = DatasetFactoryEx.createInsertOrderPreservingDataset();
        readAsGiven(result, uri, baseIri);
        return result;
    }


    public static void writeAsGiven(OutputStream out, Dataset dataset, RDFFormat rdfFormat, String baseIri) {
        writeAsGiven(out, dataset.asDatasetGraph(), rdfFormat, baseIri);
    }

    // TODO Implement; A variant of write that accepts a context; allows e.g. disabling writing out base IRIs
    public static void writeAsGiven(OutputStream out, DatasetGraph datasetGraph, RDFFormat rdfFormat, String baseIri) {
        Context cxt = RIOT.getContext().copy();
        cxt.setTrue(RIOT.symTurtleOmitBase);

        StreamRDF writer = StreamRDFWriterEx.getWriterStream(
                out,
                rdfFormat,
                cxt,
                null,
                NodeToLabel.createBNodeByLabelAsGiven(),
                true
        );

        if (baseIri != null) {
            // IRIx irix = IRIx.createAny(baseIri);
            IRIx irix = IRIxResolverUtils.newIRIxAsGiven(baseIri);
            WriterStreamRDFBaseUtils.setNodeFormatterIRIx((WriterStreamRDFBase)writer, irix);
        }


        writer.start();
        StreamRDFOps.sendDatasetToStream(datasetGraph, writer);
        writer.finish();


//        RDFWriter writer = RDFWriter
//            .create(dataset)
//            .base(baseIri)
//            .context(cxt)
//            .format(rdfFormat)
//            .build();
//
//        if (writer instanceof WriterStreamRDF) {
//            // WriterStreamRDFBaseUtils.setNodeToLabel(writer, RDFDataMgrRx.createLabelToNodeAsGivenOrRandom());
//        }
            //.output(out);

        // RDFDataMgr.write
        // Context.set(RIOT.symTurtleOmitBase);
        // RIOT.multilineLiterals
        // TODO
    }
}
