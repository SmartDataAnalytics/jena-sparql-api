package org.aksw.jena_sparql_api.rx;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.ext.com.google.common.collect.ArrayListMultimap;
import org.apache.jena.ext.com.google.common.collect.Multimap;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.shared.PrefixMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.rxjava3.core.Flowable;

/**
 * Extensions to load models from .sparql files
 *
 * @author Claus Stadler, Dec 18, 2018
 *
 */
public class RDFDataMgrEx {
    private static final Logger logger = LoggerFactory.getLogger(RDFDataMgrEx.class);

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
    public static TypedInputStream probeLang(InputStream in, Iterable<Lang> candidates, boolean tryAllCandidates) {
        BufferedInputStream bin = new BufferedInputStream(in);

        Multimap<Long, Lang> successCountToLang = ArrayListMultimap.create();
        for(Lang cand : candidates) {
            @SuppressWarnings("resource")
            CloseShieldInputStream wbin = new CloseShieldInputStream(bin);

            // Here we rely on the VM/JDK not allocating the buffer right away but only
            // using this as the max buffer size
            // 1GB should be safe enough even for cases with huge literals such as for
            // large spatial geometries (I encountered some around ~50MB)
            bin.mark(1 * 1024 * 1024 * 1024);
            //bin.mark(Integer.MAX_VALUE >> 1);
            Flowable<?> flow;
            if(RDFLanguages.isQuads(cand)) {
                flow = RDFDataMgrRx.createFlowableQuads(() -> wbin, cand, null);
            } else if(RDFLanguages.isTriples(cand)) {
                flow = RDFDataMgrRx.createFlowableTriples(() -> wbin, cand, null);
            } else {
                logger.warn("Skipping probing of unknown Lang: " + cand);
                continue;
            }

            try {
                long count = flow.take(1000)
                    .count()
                    .blockingGet();

                successCountToLang.put(count, cand);

                logger.debug("Number of items parsed by content type probing for " + cand + ": " + count);
            } catch(Exception e) {
                continue;
            } finally {
                try {
                    bin.reset();
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
        TypedInputStream result = new TypedInputStream(bin, bestContentType);

        return result;
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

        boolean useStdIn = "-".equals(src);

        TypedInputStream result;
        if(useStdIn) {
            // Use the close shield to prevent closing stdin on .close()
            // TODO Investigate if this is redundant; RDFDataMgr might already do it
            result = probeLang(new CloseShieldInputStream(System.in), probeLangs);
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
                result = probeLang(result.getInputStream(), probeLangs);
            }

        }

        return result;
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

}
