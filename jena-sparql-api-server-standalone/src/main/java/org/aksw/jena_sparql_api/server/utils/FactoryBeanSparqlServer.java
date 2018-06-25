package org.aksw.jena_sparql_api.server.utils;

import java.util.function.Function;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.SparqlServiceFactory;
import org.aksw.jena_sparql_api.core.SparqlServiceImpl;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.jena_sparql_api.web.server.ServerUtils;
import org.apache.http.client.HttpClient;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.DatasetDescription;
import org.eclipse.jetty.server.Server;
import org.springframework.web.context.support.GenericWebApplicationContext;

public class FactoryBeanSparqlServer {
    protected Integer port;
    protected SparqlServiceFactory sparqlServiceFactory;
    protected Function<String, SparqlStmt> sparqlStmtParser;

    public int getPort() {
        return port;
    }

    public FactoryBeanSparqlServer setPort(int port) {
        this.port = port;

        return this;
    }

    public SparqlServiceFactory getSparqlServiceFactory() {
        return sparqlServiceFactory;
    }

    public FactoryBeanSparqlServer setSparqlServiceFactory(SparqlServiceFactory sparqlServiceFactory) {
        this.sparqlServiceFactory = sparqlServiceFactory;

        return this;
    }

    public FactoryBeanSparqlServer setSparqlServiceFactory(QueryExecutionFactory qef) {
        SparqlServiceFactory ssf = new SparqlServiceFactory() {
            @Override
            public SparqlService createSparqlService(String serviecUri, DatasetDescription datasetDescription, HttpClient httpClient) {
                 return new SparqlServiceImpl(qef, null);
            }
        };

        this.setSparqlServiceFactory(ssf);

        return this;
    }

    public Function<String, SparqlStmt> getSparqlStmtParser() {
        return sparqlStmtParser;
    }

    public FactoryBeanSparqlServer setSparqlStmtParser(Function<String, SparqlStmt> sparqlStmtParser) {
        this.sparqlStmtParser = sparqlStmtParser;

        return this;
    }


    public Server create() {
        if(port == null) {
            port = 7531;
        }

        if(sparqlStmtParser == null) {
            sparqlStmtParser = SparqlStmtParserImpl.create(Syntax.syntaxARQ, true);
        }

        if(sparqlServiceFactory == null) {
            throw new RuntimeException("SparqlServiceFactory must not be null");
        }

        GenericWebApplicationContext rootContext = new GenericWebApplicationContext();
        
        rootContext.getBeanFactory().registerSingleton("sparqlServiceFactory", sparqlServiceFactory);
        rootContext.getBeanFactory().registerSingleton("sparqlStmtParser", sparqlStmtParser);

        Server result = ServerUtils.startServer(port, new WebAppInitializerSparqlService(rootContext));
        return result;
    }

    public static FactoryBeanSparqlServer newInstance() {
        return new FactoryBeanSparqlServer();
    }
}
