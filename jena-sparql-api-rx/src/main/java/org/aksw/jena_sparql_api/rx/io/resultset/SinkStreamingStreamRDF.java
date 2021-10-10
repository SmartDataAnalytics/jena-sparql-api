package org.aksw.jena_sparql_api.rx.io.resultset;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;

public class SinkStreamingStreamRDF
    extends SinkStreamingBase<Quad>
    implements StreamRDF
{
    protected StreamRDF streamRDF;
    // protected OutputStream out;

    public SinkStreamingStreamRDF(StreamRDF streamRDF) {
        super();
        this.streamRDF = streamRDF;
    }

    @Override
    public void flush() {
        // Method has no equivalent on streamRDF
    }

    @Override
    public void close() {
        // No op
    }

    @Override
    protected void sendActual(Quad quad) {
        streamRDF.quad(quad);
    }

    @Override
    protected void startActual() {
        streamRDF.start();
//
//        // send out the header
//        if (prefixesAndHeader != null) {
//            StreamRDFOps.sendDatasetToStream(prefixesAndHeader.asDatasetGraph(), this);
//        }
    }

    @Override
    protected void finishActual() {
        streamRDF.finish();
    }

    @Override
    public void triple(Triple triple) {
        Quad quad = Quad.create(Quad.defaultGraphNodeGenerated, triple);
        sendActual(quad);
    }

    @Override
    public void quad(Quad quad) {
        sendActual(quad);
    }

    @Override
    public void base(String base) {
        streamRDF.base(base);
    }

    @Override
    public void prefix(String prefix, String iri) {
        streamRDF.prefix(prefix, iri);
    }
}
