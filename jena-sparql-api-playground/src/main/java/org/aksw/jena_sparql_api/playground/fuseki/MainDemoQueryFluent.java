package org.aksw.jena_sparql_api.playground.fuseki;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.cache.file.CacheBackendFile;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.apache.jena.rdfconnection.RDFConnection;


public class MainDemoQueryFluent {
    public static void main(String[] args) {

        SparqlService ss = FluentSparqlService
            .http("https://databus.dbpedia.org/repo/sparql")
            .config()
                .configQuery()
                    .withDelay(1, TimeUnit.SECONDS)
                    // .withCache(new CacheBackendFile(Paths.get("/tmp/cache"), 600000l, true, false, true))
                    .withPagination(100)
                    .withDefaultLimit(10, true)
                .end()
            .end()
            .create();

        try(RDFConnection baseConn = ss.getRDFConnection()) {
          try(RDFConnection appConn = baseConn) {
                String queryStr = "SELECT * { ?s a <http://dataid.dbpedia.org/ns/core#DataId> ; <http://dataid.dbpedia.org/ns/core#associatedAgent> <https://vehnem.github.io/webid.ttl#this> }";
                SparqlRx.execSelect(appConn, queryStr)
                    .forEach(qs -> System.out.println(qs));
            }
        }
    }
}
