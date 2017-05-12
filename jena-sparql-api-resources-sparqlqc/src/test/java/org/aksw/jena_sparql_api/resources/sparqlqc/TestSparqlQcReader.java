package org.aksw.jena_sparql_api.resources.sparqlqc;

import java.io.IOException;
import java.util.List;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.junit.Test;

public class TestSparqlQcReader {

    @Test
    public void testReadSchema() throws IOException {
        List<Resource> tasks = SparqlQcReader.loadTasks("sparqlqc/1.4/benchmark/ucqrdfs.rdf");// ;, "sparqlqc/1.4/benchmark/rdfs/*");
        //List<Resource> tasks = SparqlQcReader.loadTasks("sparqlqc/1.4/benchmark/cqnoproj.rdf", "sparqlqc/1.4/benchmark/noprojection/*");
        for(Resource task : tasks) {
            RDFDataMgr.write(System.out, task.getModel(), RDFFormat.TURTLE);
        }
    }
}
