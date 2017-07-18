package org.aksw.jena_sparql_api.resources.sparqlqc;

import java.io.IOException;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

public class MainRdfizeSparqlQc {
    public static void main(String[] args) throws IOException {
        Model model = ModelFactory.createDefaultModel();

        List<Resource> suites = SparqlQcReader.loadTestSuites("sparqlqc/1.4/benchmark/cqnoproj.rdf");

        suites.forEach(suite -> model.add(suite.getModel()));

        RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
    }
}
