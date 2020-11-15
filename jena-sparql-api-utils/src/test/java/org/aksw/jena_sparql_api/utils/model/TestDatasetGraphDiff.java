package org.aksw.jena_sparql_api.utils.model;

import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Test;

public class TestDatasetGraphDiff {

    @Test
    public void test() {
        DatasetGraphDiff dg = new DatasetGraphDiff();

        dg.addGraph(SSE.parseNode(":g"), GraphFactory.createDefaultGraph());
        dg.add(SSE.parseQuad("(quad :g :s :p :o)"));

        dg.begin(ReadWrite.WRITE);
        dg.materialize();

        //dg.abort();
        dg.commit();

        System.err.println("Effective Dataset:");
        System.err.println("-----------------");
        RDFDataMgr.write(System.err, DatasetFactory.wrap(dg), RDFFormat.TRIG_BLOCKS);

        System.err.println("Added Quads:");
        System.err.println("-----------------");
        RDFDataMgr.write(System.err, DatasetFactory.wrap(dg.getAdded()), RDFFormat.TRIG_BLOCKS);

        System.err.println("Removed Quads:");
        System.err.println("-----------------");
        RDFDataMgr.write(System.err, DatasetFactory.wrap(dg.getRemoved()), RDFFormat.TRIG_BLOCKS);

        System.err.println("Added Graphs: " + dg.getAddedGraphs());
        System.err.println("Removed Graphs:" + dg.getRemovedGraphs());

    }
}
