package org.aksw.jena_sparql_api.rx.io.resultset;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.util.iri.PrefixUtils;
import org.aksw.jena_sparql_api.utils.GraphUtils;
import org.aksw.jena_sparql_api.utils.io.StreamRDFDeferred;
import org.apache.jena.ext.com.google.common.collect.Iterators;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.lang.SinkQuadsToDataset;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.Quad;

public class SinkStreamingQuads
{

    /**
     * Create a sink that for line based format
     * streams directly to the output stream or collects quads in memory and emits them
     * all at once in the given format when flushing the sink.
     *
     * @param r
     * @param format
     * @param out
     * @param prefixAnalysisEventCount Defer output until this number of triple/quad events have
     *        been reached and optimize prefixes for the seen data in that window.
     *        Negative values cause deferring until all events have been seen which effectively disables streaming.
     *        A value of zero immediately prints out the prefixes
     * @param dataset The dataset implementation to use for non-streaming data.
     *                Allows for use of insert-order preserving dataset implementations.
     * @return
     */
    public static SinkStreaming<Quad> createSinkQuads(
    		RDFFormat format,
    		OutputStream out,
    		PrefixMapping pm,
    		long prefixAnalysis,
    		Supplier<Dataset> datasetSupp) {
//        boolean useStreaming = format == null ||
//                Arrays.asList(Lang.NTRIPLES, Lang.NQUADS).contains(format.getLang());
        SinkStreaming<Quad> result;

        boolean useStreaming = true;
        if(useStreaming) {
            StreamRDF writer = StreamRDFWriter.getWriterStream(out, format, null);
            writer = new StreamRDFDeferred(writer, true, pm, prefixAnalysis, 100 * prefixAnalysis, null);
//            Dataset header = DatasetFactory.create();
//            header.getDefaultModel().setNsPrefixes(pm);
            result = new SinkStreamingStreamRDF(writer);

            // result = SinkStreamingWrapper.wrap(new SinkQuadOutput(out, null, null));
        } else {
            Dataset dataset = datasetSupp.get();
            SinkQuadsToDataset core = new SinkQuadsToDataset(false, dataset.asDatasetGraph());

            return new SinkStreamingBase<Quad>() {
                @Override
                public void close() {
                    core.close();
                }

                @Override
                public void sendActual(Quad item) {
                    core.send(item);
                }

                @Override
                public void flush() {
                    core.flush();
                }

                @Override
                public void finishActual() {
                    // TODO Prefixed graph names may break
                    // (where to define their namespace anyway? - e.g. in the default or the named graph?)
                    PrefixMapping usedPrefixes = new PrefixMappingImpl();

                    Stream.concat(
                            Stream.of(dataset.getDefaultModel()),
                            Streams.stream(dataset.listNames()).map(dataset::getNamedModel))
                    .forEach(m -> {
                        // PrefixMapping usedPrefixes = new PrefixMappingImpl();
                        try(Stream<Node> nodeStream = GraphUtils.streamNodes(m.getGraph())) {
                            PrefixUtils.usedPrefixes(pm, nodeStream, usedPrefixes);
                        }
                        m.clearNsPrefixMap();
                        // m.setNsPrefixes(usedPrefixes);
                    });

                    dataset.getDefaultModel().setNsPrefixes(usedPrefixes);

                    if (RDFLanguages.isTriples(format.getLang())) {
                        Iterator<String> it = dataset.listNames();
                        if (it.hasNext()) {
                            int maxShow = 5;
                            List<String> graphNames = Streams.stream(it).limit(maxShow).collect(Collectors.toList());
                            int headCount = graphNames.size();
                            int totalCount = headCount + Iterators.size(it);

                            throw new RuntimeException("Requested triple-based format " + format + " but named graphs in dataset. Showing " + headCount + " out of " + totalCount + ": " + graphNames);
                        }

                        RDFDataMgr.write(out, dataset.getDefaultModel(), format);
                    } else {
                        RDFDataMgr.write(out, dataset, format);
                    }
                }
            };
        }

        return result;
    }

}