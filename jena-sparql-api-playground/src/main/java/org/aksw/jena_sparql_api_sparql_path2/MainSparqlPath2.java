package org.aksw.jena_sparql_api_sparql_path2;

import org.aksw.jena_sparql_api.core.GraphSparqlService;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.SparqlServiceFactory;
import org.aksw.jena_sparql_api.stmt.SparqlParserConfig;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.aksw.jena_sparql_api.update.FluentSparqlServiceFactory;
import org.aksw.jena_sparql_api.web.server.ServerUtils;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.query.ARQ;
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

import rx.Observable;


public class MainSparqlPath2 {

    public static SparqlService wrapSparqlService(SparqlService coreSparqlService, SparqlStmtParserImpl sparqlStmtParser, Prologue prologue) {

        GraphSparqlService graph = new GraphSparqlService(coreSparqlService);
        Model model = ModelFactory.createModelForGraph(graph);

        Context context = ARQ.getContext().copy();

        SparqlService result = FluentSparqlService
                .from(model, context)
                .config()
                    .configQuery()
                        .withParser(sparqlStmtParser.getQueryParser())
                        .withPrefixes(prologue.getPrefixMapping(), true) // If a query object is without prefixes, inject them
                        .end()
                    .end()
                .create();


        context.put(PropertyFunctionKShortestPaths.PROLOGUE, prologue);
        context.put(PropertyFunctionKShortestPaths.SPARQL_SERVICE, coreSparqlService);

        return result;
    }


    public static void main(String[] args) throws InterruptedException {

        //Observable.from(args).subscribe()
        Observable<Object> obs = Observable.create(subscriber -> {
            for(int i = 0; i < 50; ++i) {
                if(!subscriber.isUnsubscribed()) {
                    subscriber.onNext("yay" + i);
                }
            }
            if(!subscriber.isUnsubscribed()) {
                subscriber.onCompleted();
            }
        });
        obs.subscribe(x -> System.out.println(x));

        if(true) {
            return;
        }

        PropertyFunctionRegistry.get().put(PropertyFunctionKShortestPaths.DEFAULT_IRI, new PropertyFunctionFactoryKShortestPaths());

        //SparqlService coreSparqlService = FluentSparqlService.http("http://fp7-pp.publicdata.eu/sparql", "http://fp7-pp.publicdata.eu/").create();
        //SparqlService coreSparqlService = FluentSparqlService.http("http://localhost:8890/sparql", "http://fp7-pp.publicdata.eu/").create();
        //FluentSparqlServiceFactoryFn.start().configService().

        //SparqlService coreSparqlService = FluentSparqlService.http("http://dbpedia.org/sparql", "http://dbpedia.org").create();

        PrefixMappingImpl pm = new PrefixMappingImpl();
        pm.setNsPrefix("jsafn", "http://jsa.aksw.org/fn/");
        pm.setNsPrefixes(PrefixMapping.Extended);
        Prologue prologue = new Prologue(pm);

        SparqlStmtParserImpl sparqlStmtParser = SparqlStmtParserImpl.create(SparqlParserConfig.create(Syntax.syntaxARQ, prologue));


        SparqlServiceFactory ssf = new SparqlServiceFactory() {
            @Override
            public SparqlService createSparqlService(String serviceUri,
                    DatasetDescription datasetDescription, Object authenticator) {

                SparqlService coreSparqlService = FluentSparqlService.http(serviceUri, datasetDescription, (HttpAuthenticator)authenticator).create();
                SparqlService r = wrapSparqlService(coreSparqlService, sparqlStmtParser, prologue);
                return r;
            }
        };

        ssf = FluentSparqlServiceFactory.from(ssf)
                .configFactory()
                    //.defaultServiceUri("http://dbpedia.org/sparql")
                    .defaultServiceUri("http://localhost:8890/sparql")
                    .configService()
                        .configQuery()
                            .withPagination(1000)
                        .end()
                    .end()
                .end()
                .create();

        Server server = ServerUtils.startSparqlEndpoint(ssf, sparqlStmtParser, 7533);
        server.join();


        // Create a datasetGraph backed by the SPARQL service to DBpedia
//        DatasetGraphSparqlService datasetGraph = new DatasetGraphSparqlService(coreSparqlService);

        // TODO Add support for sparqlService transformation
//        final SparqlServiceFactory ssf = FluentSparqlServiceFactory.from(new SparqlServiceFactoryHttp())
//            .configFactory()
//                .defaultServiceUri("http://dbpedia.org/sparql")
//                .configService()
//                    .configQuery()
//                        .withParser(sparqlStmtParser.getQueryParser())
//                        .withPrefixes(pm, true) // If a query object is without prefixes, inject them
//                    .end()
//                .end()
//            .end()
//            .create();



//        SparqlServiceFactory ssf = new SparqlServiceFactory() {
//            @Override
//            public SparqlService createSparqlService(String serviceUri,
//                    DatasetDescription datasetDescription,
//                    Object authenticator) {
//                return sparqlService;
//            }
//
//        };



        //Model model = ModelFactory.createDefaultModel();
        //GraphQueryExecutionFactory

        //String queryStr = "SELECT * { ?s ?p ?o } LIMIT 10";
//
        //String queryStr = "SELECT ?path { <http://fp7-pp.publicdata.eu/resource/project/257943> jsafn:kShortestPaths ('(rdf:type|!rdf:type)*' ?path <http://fp7-pp.publicdata.eu/resource/city/France-PARIS>) }";
//        String queryStr = "SELECT ?path { <http://fp7-pp.publicdata.eu/resource/project/257943> jsafn:kShortestPaths ('rdf:type*' ?path) }";
//        //QueryExecutionFactory qef = FluentQueryExecutionFactory.http("http://dbpedia.org/sparql", "http://dbpedia.org").create();
//
//        for(int i = 0; i < 1; ++i) {
//            QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();
//            QueryExecution qe = qef.createQueryExecution(queryStr);
////            //System.out.println("query: " + qe.getQuery());
//            System.out.println("Result");
//            ResultSet rs = qe.execSelect();
//            System.out.println(ResultSetFormatter.asText(rs));
//            //ResultSetFormatter.outputAsTSV(System.out, rs);
//        }

      //Thread.sleep(1000);
    }


}
