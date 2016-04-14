package org.aksw.jena_sparql_api.server.utils;

import org.aksw.jena_sparql_api.core.SparqlServiceFactory;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParser;
import org.aksw.jena_sparql_api.web.server.ServerUtils;
import org.eclipse.jetty.server.Server;
import org.springframework.web.context.support.GenericWebApplicationContext;

public class SparqlServerUtils {

    public static Server startSparqlEndpoint(SparqlServiceFactory ssf, SparqlStmtParser sparqlStmtParser, int port) {
//      AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
//      rootContext.refresh();
      GenericWebApplicationContext rootContext = new GenericWebApplicationContext();
      rootContext.getBeanFactory().registerSingleton("sparqlServiceFactory", ssf);
      rootContext.getBeanFactory().registerSingleton("sparqlStmtParser", sparqlStmtParser);

      Server result = ServerUtils.startServer(port, new WebAppInitializerSparqlService(rootContext));
      return result;
  }

}
