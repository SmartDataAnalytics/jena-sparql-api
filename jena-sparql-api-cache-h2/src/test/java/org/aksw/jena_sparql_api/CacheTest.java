package org.aksw.jena_sparql_api;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.delay.core.QueryExecutionFactoryDelay;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;
import org.aksw.jena_sparql_api.retry.core.QueryExecutionFactoryRetry;
import org.junit.Test;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;

public class CacheTest {
    
//    @Test
//    public void test() {
//        QueryExecutionFactory qef = new QueryExecutionFactoryHttp("http://dbpedia.org/sparql", "http://dbpedia.org");
//        
//        qef = new QueryExecutionFactoryRetry(qef, 5, 10000);
//        
//        // Add delay in order to be nice to the remote server (delay in milli seconds)
//        qef = new QueryExecutionFactoryDelay(qef, 5000);
//
//        // Set up a cache
//        // Cache entries are valid for 1 day
//        long timeToLive = 24l * 60l * 60l * 1000l; 
//        
//        // This creates a 'cache' folder, with a database file named 'sparql.db'
//        // Technical note: the cacheBackend's purpose is to only deal with streams,
//        // whereas the frontend interfaces with higher level classes - i.e. ResultSet and Model
//
////      CacheBackend cacheBackend = CacheCoreH2.create("sparql", timeToLive, true);
////      CacheFrontend cacheFrontend = new CacheFrontendImpl(cacheBackend);      
////      qef = new QueryExecutionFactoryCacheEx(qef, cacheFrontend);
// 
//        
//
//        QueryExecutionFactoryHttp foo = qef.unwrap(QueryExecutionFactoryHttp.class);
//        System.out.println(foo);
//        
//        // Add pagination
//        qef = new QueryExecutionFactoryPaginated(qef, 900);
//
//        // Create a QueryExecution object from a query string ...
//        QueryExecution qe = qef.createQueryExecution("Select ?s { ?s a <http://dbpedia.org/ontology/City> } Limit 5000");
//        
//        
//        // and run it.
//        ResultSet rs = qe.execSelect();
//        System.out.println(ResultSetFormatter.asText(rs));
//    }
//    
}
