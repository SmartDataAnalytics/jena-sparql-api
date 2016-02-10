package org.aksw.jena_sparql_api_sparql_path2;

import org.aksw.jena_sparql_api.core.GraphSparqlService;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.SparqlServiceFactory;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.aksw.jena_sparql_api.update.FluentSparqlServiceFactory;
import org.aksw.jena_sparql_api.update.FluentSparqlServiceFactoryFn;
import org.aksw.jena_sparql_api.web.server.ServerUtils;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;
import org.apache.jena.sparql.util.Context;
import org.eclipse.jetty.server.Server;


public class MainSparqlPath2 {


    public static void main(String[] args) throws InterruptedException {

        PropertyFunctionRegistry.get().put(PropertyFunctionKShortestPaths.DEFAULT_IRI, new PropertyFunctionFactoryKShortestPaths());

        //SparqlService coreSparqlService = FluentSparqlService.http("http://fp7-pp.publicdata.eu/sparql", "http://fp7-pp.publicdata.eu/").create();
        //SparqlService coreSparqlService = FluentSparqlService.http("http://localhost:8890/sparql", "http://fp7-pp.publicdata.eu/").create();
        //FluentSparqlServiceFactoryFn.start().configService().

        SparqlService coreSparqlService = FluentSparqlService.http("http://dbpedia.org/sparql", "http://dbpedia.org").create();

        // Create a datasetGraph backed by the SPARQL service to DBpedia
//        DatasetGraphSparqlService datasetGraph = new DatasetGraphSparqlService(coreSparqlService);

        GraphSparqlService graph = new GraphSparqlService(coreSparqlService);
        Model model = ModelFactory.createModelForGraph(graph);

        Context context = ARQ.getContext().copy();
        //SymbolRegistry.
        PrefixMappingImpl pm = new PrefixMappingImpl();
        pm.setNsPrefix("jsafn", "http://jsa.aksw.org/fn/");
        pm.setNsPrefixes(PrefixMapping.Extended);
        Prologue prologue = new Prologue(pm);

        final SparqlService sparqlService = FluentSparqlService
                .from(model, context)
                .config()
                    .configQuery()
                        .withParser(SparqlQueryParserImpl.create(Syntax.syntaxARQ, prologue))
                        .withPrefixes(pm, true)
                        .end()
                    .end()
                .create();


        context.put(PropertyFunctionKShortestPaths.PROLOGUE, prologue);
        context.put(PropertyFunctionKShortestPaths.SPARQL_SERVICE, coreSparqlService);


        SparqlServiceFactory ssf = new SparqlServiceFactory() {
            @Override
            public SparqlService createSparqlService(String serviceUri,
                    DatasetDescription datasetDescription,
                    Object authenticator) {
                return sparqlService;
            }

        };

        Server server = ServerUtils.startSparqlEndpoint(ssf, 7533);
        server.join();


        //Model model = ModelFactory.createDefaultModel();
        //GraphQueryExecutionFactory

        //String queryStr = "SELECT * { ?s ?p ?o } LIMIT 10";
//
        //String queryStr = "SELECT ?path { <http://fp7-pp.publicdata.eu/resource/project/257943> jsafn:kShortestPaths ('(rdf:type|!rdf:type)*' ?path <http://fp7-pp.publicdata.eu/resource/city/France-PARIS>) }";
        String queryStr = "SELECT ?path { <http://fp7-pp.publicdata.eu/resource/project/257943> jsafn:kShortestPaths ('rdf:type*' ?path) }";
//        //QueryExecutionFactory qef = FluentQueryExecutionFactory.http("http://dbpedia.org/sparql", "http://dbpedia.org").create();
//
//        for(int i = 0; i < 1; ++i) {
            QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();
            QueryExecution qe = qef.createQueryExecution(queryStr);
//            //System.out.println("query: " + qe.getQuery());
            System.out.println("Result");
            ResultSet rs = qe.execSelect();
            System.out.println(ResultSetFormatter.asText(rs));
//            //ResultSetFormatter.outputAsTSV(System.out, rs);
//        }

      //Thread.sleep(1000);
    }


}
