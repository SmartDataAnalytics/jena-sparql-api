package org.aksw.jena_sparql_api.core;

import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.stmt.SparqlParserConfig;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.apache.http.client.HttpClient;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;

public class SparqlServiceUtils {

    public static SparqlService createSparqlServiceMem(String serviceUri, DatasetDescription datasetDescription, HttpClient httpClient) {
    	DatasetGraph dg = DatasetGraphFactory.create();
        final SparqlService result = FluentSparqlService.from(dg)
            .config()
                .withParser(SparqlStmtParserImpl.create(SparqlParserConfig.create(Syntax.syntaxARQ)))
                .withDatasetDescription(datasetDescription)
            .end()
            .create();

//        new Thread(() -> {
//            result.getUpdateExecutionFactory().createUpdateProcessor("PREFIX ex: <http://example.org/> INSERT DATA { ex:s ex:p ex:o }").execute();
//        }).start();
//
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        new Thread(() -> {
//            result.getQueryExecutionFactory().createQueryExecution("CONSTRUCT WHERE { ?s ?p ?o }").execConstruct().write(System.out, "TTL");
//        }).start();
//
//
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        if(true) {
//            System.exit(0);
//        }

//
//        result.getQueryExecutionFactory().createQueryExecution("CONSTRUCT WHERE { ?s ?p ?o }").execConstruct().write(System.out);

        return result;
    }

    public static SparqlService createSparqlService(String serviceUri, DatasetDescription datasetDescription, HttpClient httpClient) {
        SparqlService result;
        if(serviceUri != null && serviceUri.startsWith("mem://")) {
            result = createSparqlServiceMem(serviceUri, datasetDescription, httpClient);
        } else {
            result = createSparqlServiceHttp(serviceUri, datasetDescription, httpClient);
        }

        return result;
    }

    public static SparqlService createSparqlServiceHttp(String serviceUri, DatasetDescription datasetDescription, HttpClient httpClient) {




//        if(authenticator != null && !(authenticator instanceof HttpAuthenticator)) {
//            throw new RuntimeException("Authenticator is not an instance of " + HttpAuthenticator.class.getCanonicalName());
//        }

//        HttpAuthenticator httpAuthenticator = (HttpAuthenticator)authenticator;
        QueryExecutionFactoryHttp qef = new QueryExecutionFactoryHttp(serviceUri, datasetDescription, httpClient);
        UpdateExecutionFactoryHttp uef = new UpdateExecutionFactoryHttp(serviceUri, datasetDescription, httpClient);

        SparqlService result = new SparqlServiceImpl(serviceUri, datasetDescription, qef, uef);
        return result;
    }

}
