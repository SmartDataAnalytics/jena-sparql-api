package org.aksw.jena_sparql_api.utils.io;

import java.io.OutputStream;
import java.util.Map;

import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.out.NodeToLabel;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWrapper;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.riot.system.SyntaxLabels;
import org.apache.jena.riot.writer.WriterStreamRDFBase;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.Context;

/**
 * Special purpose RDF writer generation.
 * Especially blank nodes are preserved as given.
 *
 * @author raven
 *
 */
public class StreamRDFWriterEx {

    public static StreamRDF getWriterStream(
            OutputStream out,
            RDFFormat rdfFormat) {
        return getWriterStream(out, rdfFormat, null, null, null, null);
    }


    /**
     * Create a StreamRDF writer with extended options.
     *
     * @param out The output stream.
     * @param rdfFormat The rdf format; a registration of a streamable writer must exist for it
     * @param context The context passed to the writer creation.
     * @param fixedPrefixes If non-null, only this set prefixes will be written out;
     *            the returned writer will ignore prefix events.
     * @param nodeToLabel The blank node strategy. If null, <b>blank nodes are preserved as given</b>.
     * @param mapQuadsToTriplesForTripleLangs If false, the writer for a quad language will ignore triples.
     *            If true, triples become quads in the default graph. Defaults to true.
     * @return A writer according to parameterization.
     */
    public static StreamRDF getWriterStream(
            OutputStream out,
            RDFFormat rdfFormat,
            Context context,
            PrefixMapping fixedPrefixes,
            NodeToLabel nodeToLabel,
            Boolean mapQuadsToTriplesForTripleLangs
    ) {
        StreamRDF rawWriter = StreamRDFWriter.getWriterStream(out, rdfFormat, context);

        StreamRDF coreWriter = StreamRDFUtils.unwrap(rawWriter);

        // Retain blank nodes as given
        if (coreWriter instanceof WriterStreamRDFBase) {
            WriterStreamRDFBase tmp = (WriterStreamRDFBase)coreWriter;

            NodeToLabel effectiveNodeToLabel = nodeToLabel == null
                    ? SyntaxLabels.createNodeToLabelAsGiven()
                    : nodeToLabel;

            WriterStreamRDFBaseUtils.setNodeToLabel(tmp, effectiveNodeToLabel);

            if (fixedPrefixes != null) {
                PrefixMap pm = WriterStreamRDFBaseUtils.getPrefixMap(tmp);
                for (Map.Entry<String, String> e : fixedPrefixes.getNsPrefixMap().entrySet()) {
                    pm.add(e.getKey(), e.getValue());
                }

                rawWriter = StreamRDFUtils.wrapWithoutPrefixDelegation(rawWriter);
            }
        }

        if (Boolean.TRUE.equals(mapQuadsToTriplesForTripleLangs) && RDFLanguages.isTriples(rdfFormat.getLang())) {
            rawWriter = new StreamRDFWrapper(rawWriter) {
                @Override
                public void quad(Quad quad) {
                    super.triple(quad.asTriple());
                }
            };
        }

        return rawWriter;
    }
}
