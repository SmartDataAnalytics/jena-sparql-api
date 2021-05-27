package org.aksw.jena_sparql_api.conjure.datapod.impl;

import org.aksw.commons.util.ref.Ref;
import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.aksw.jena_sparql_api.io.hdt.HDTHeaderGraph;
import org.aksw.jena_sparql_api.utils.GraphUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdtjena.HDTGraph;

public class RdfDataPodHdtImpl
    implements RdfDataPodHdt
{
    /**
     * Reference to HDT resources
     *
     * TODO Put the HDT *and* the HDTGraph into a common object,
     * so that if the HDTGraph only needs to be initialized once for
     * any number of requests
     */
    protected Ref<HDT> hdtRef;
    protected boolean isHeaderPod;


    public RdfDataPodHdtImpl(Ref<HDT> hdtRef, boolean isHeaderPod) {
        super();
        this.hdtRef = hdtRef;
        this.isHeaderPod = isHeaderPod;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public void close() throws Exception {
        hdtRef.close();
    }

    @Override
    public RDFConnection openConnection() {
        HDT hdt = hdtRef.get();

        Graph graph;
        if(isHeaderPod) {
            graph = new HDTHeaderGraph(hdt);
        } else {
            graph = new HDTGraph(hdt);

            // TODO Evaluate to what extend this fix is useful
            // A conclusion might be, that the input data itself should be fixed
            // instead of us attempting a workaround here
            boolean enableNQuadsFix = true;
            if(enableNQuadsFix) {
                graph = GraphUtils.wrapGraphWithNQuadsFix(graph);
                graph = GraphUtils.wrapWithValidation(graph);
            }
        }

        Model model = ModelFactory.createModelForGraph(graph);

        RDFConnection result = RDFConnectionFactory.connect(DatasetFactory.wrap(model));
        return result;

    }

    @Override
    public RdfDataPod headerPod() {
        Ref<HDT> freshRef = hdtRef.acquire(this);
        if(isHeaderPod) {
            throw new RuntimeException("Cannot get header of a header");
        } else {
            return new RdfDataPodHdtImpl(freshRef, true);
        }
        //return this; // or null ? or a pod with an empty model?
    }
}
