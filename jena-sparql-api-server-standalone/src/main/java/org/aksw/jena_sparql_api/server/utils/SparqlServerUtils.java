package org.aksw.jena_sparql_api.server.utils;

import org.aksw.jena_sparql_api.core.SparqlServiceFactory;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParser;
import org.eclipse.jetty.server.Server;

public class SparqlServerUtils {

    /**
     * Use FactoryBeanSparqlServer instead
     *
     * @param ssf
     * @param sparqlStmtParser
     * @param port
     * @return
     */
    @Deprecated
    public static Server startSparqlEndpoint(SparqlServiceFactory ssf, SparqlStmtParser sparqlStmtParser, int port) {
        Server result = FactoryBeanSparqlServer.newInstance()
                .setPort(port)
                .setSparqlStmtParser(sparqlStmtParser)
                .setSparqlServiceFactory(ssf).create();
        return result;

//      AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
//      rootContext.refresh();
//      GenericWebApplicationContext rootContext = new GenericWebApplicationContext();
//      rootContext.getBeanFactory().registerSingleton("sparqlServiceFactory", ssf);
//      rootContext.getBeanFactory().registerSingleton("sparqlStmtParser", sparqlStmtParser);
//
//      Server result = ServerUtils.startServer(port, new WebAppInitializerSparqlService(rootContext));
  }

}
