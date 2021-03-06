package org.aksw.jena_sparql_api.example.views;

import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.SparqlServiceFactory;
import org.aksw.jena_sparql_api.core.SparqlServiceImpl;
import org.aksw.jena_sparql_api.http.QueryExecutionHttpWrapper;
import org.aksw.jena_sparql_api.server.utils.SparqlServerUtils;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParser;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.jena_sparql_api.views.CandidateViewSelector;
import org.aksw.jena_sparql_api.views.CandidateViewSelectorSparqlView;
import org.aksw.jena_sparql_api.views.Dialect;
import org.aksw.jena_sparql_api.views.QueryExecutionFactorySparqlView;
import org.aksw.jena_sparql_api.views.SparqlView;
import org.apache.http.client.HttpClient;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;


public class MainSparqlView {


    public static void main(String[] args) {

        QueryExecutionFactory xxx = FluentQueryExecutionFactory.http("http://dbpedia.org/sparql", "http://dbpedia.org")
                .config()
                    .withPostProcessor(qe ->
                            ((QueryEngineHTTP)((QueryExecutionHttpWrapper)qe).getDecoratee())
                            .setModelContentType(WebContent.contentTypeRDFXML))
                    .withPostProcessor(aoeu -> System.out.println("a" + aoeu))
                    .withParser(SparqlQueryParserImpl.create(Syntax.syntaxARQ))
                    .withDefaultLimit(10, true)
                    .withPostProcessor(aoeu -> System.out.println("b" + aoeu))
                    .withPagination(1000)
                    .withPostProcessor(aoeu -> System.out.println("c" + aoeu))
                .end()
                .create();

            Model foo = xxx.createQueryExecution("CONSTRUCT WHERE { <http://dbpedia.org/ontology/author> ?p ?o }").execConstruct();
            foo.write(System.out, "TURTLE");


        //SparqlViewSystem system = new SparqlViewSystem();

        //system.addView(SparqlView.create("MyView", QueryFactory.create("Prefix ex:<http://ex.org> Construct { ?s a ex:BigProject . ex:BigProject a ex:Type . } { ?s ex:funding ?o . Filter(?o > 1000) . }", Syntax.syntaxSPARQL_11)));
        //system.addView(SparqlView.create("MyView", QueryFactory.create("Prefix ex:<http://ex.org> Construct { ?p a ex:Facet . ?l ?p ?r . } { ?l ?p ?r . Filter(?p = <http://hasBeneficiary>) . }", Syntax.syntaxSPARQL_11)));


        //system.addView(SparqlView.create("MyView", QueryFactory.create("Prefix ft:<http://fintrans.publicdata.eu/ec/ontology/> Construct { ?t a ft:Facet . } { ?s a ?t }", Syntax.syntaxSPARQL_11)));

        //system.addView(SparqlView.create("MyView", QueryFactory.create("Prefix ft:<http://fintrans.publicdata.eu/ec/ontology/> Construct { ?s a ?t . } { ?s a ?t . }", Syntax.syntaxSPARQL_11)));
        //system.addView(SparqlView.create("MyView", QueryFactory.create("Prefix ft:<http://fintrans.publicdata.eu/ec/ontology/> Construct { ?s a ft:LabeledThing . } { ?s <"  + RDFS.label + "> ?x }", Syntax.syntaxSPARQL_11)));

        //TypeSystem typeSystem = SparqlifyCoreInit.createDefaultDatatypeSystem();
        CandidateViewSelector<SparqlView> candidateViewSelector = new CandidateViewSelectorSparqlView();

        //SparqlView sparqlView = SparqlView.create("MyView", QueryFactory.create("Construct { ?s a <http://fp7-pp.publicdata.eu/ontology/Project> ; <skos:prefLabel> ?x } { ?s a <http://fp7-pp.publicdata.eu/ontology/Project> ; <http://www.w3.org/2000/01/rdf-schema#label> ?o . Bind(concat('hello', ?o) As ?x) }", Syntax.syntaxSPARQL_11)); //?s ?p ?o . Filter(?p != <http://fp7-pp.publicdata.eu/ontology/call>) }", Syntax.syntaxSPARQL_11));
        SparqlView sparqlView = SparqlView.create("MyView", QueryFactory.create("Construct { ?s a <http://fp7-pp.publicdata.eu/ontology/Project> ; <skos:prefLabel> ?x } { ?s a <http://fp7-pp.publicdata.eu/ontology/Project> ; <http://www.w3.org/2000/01/rdf-schema#label> ?o . }", Syntax.syntaxSPARQL_11));
        //SparqlView sparqlView = SparqlView.create("MyView", "Construct { Graph ?g { ?s ?p ?o } } { ?s a <http://fp7-pp.publicdata.eu/ontology/Project> . ?s ?p ?o . Filter(?g != <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>) }");
        candidateViewSelector.addView(sparqlView);
        candidateViewSelector.addView(SparqlView.create("MyView", QueryFactory.create("Construct { ?s ?p ?o . } { ?s ?p ?o . FILTER(?s = <http://fp7-pp.publicdata.eu/resource/project/288819>) }", Syntax.syntaxSPARQL_11)));

        //QueryExecutionFactoryHttp qef = new QueryExecutionFactoryHttp("http://localhost:8890/sparql");
        QueryExecutionFactory qef = FluentQueryExecutionFactory
                .http("http://fp7-pp.publicdata.eu/sparql")
                .config()
                    .withPagination(1000)
                .end()
                .create();


        //QueryExecutionFactoryHttp qef = new QueryExecutionFactoryHttp("http://leipzig-data.de:8890/sparql");
        QueryExecutionFactorySparqlView sv = new QueryExecutionFactorySparqlView(qef, candidateViewSelector, Dialect.VIRTUOSO);



        //QueryExecution qe = sv.createQueryExecution("Prefix ft:<http://fintrans.publicdata.eu/ec/ontology/> Select Distinct ?t { ?s a ?t . }");
        //QueryExecution qe = sv.createQueryExecution("select * { ?s a <http://fp7-pp.publicdata.eu/ontology/Project> . ?s ?p ?o . }");

        System.out.println("Result");
        System.out.println(ResultSetFormatter.asText(sv.createQueryExecution("select * { ?s ?p ?o . FILTER(?s = <http://fp7-pp.publicdata.eu/resource/project/288819>)}").execSelect()));

//        if(true) { return; }

        SparqlStmtParser sparqlStmtParser = SparqlStmtParserImpl.create(Syntax.syntaxARQ, true);

        SparqlServiceFactory ssf = new SparqlServiceFactory() {
            @Override
            public SparqlService createSparqlService(String serviceUri,
                    DatasetDescription datasetDescription,
                    HttpClient httpClient) {

                return new SparqlServiceImpl(sv, null);
            }
        };


        SparqlServerUtils.startSparqlEndpoint(ssf, sparqlStmtParser, 7532);
    }

    /*
    public static void parseView(String string) {
        ConfigParser parser = new ConfigParser();
        SparqlifyConfigParser parser = new SparqlifyConfigParser(input);
        parser.constructTemplateQuads()
    }*/

}
