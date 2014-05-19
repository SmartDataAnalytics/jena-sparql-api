package org.aksw.jena_sparql_api.sparql_path.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;
import org.aksw.jena_sparql_api.retry.core.QueryExecutionFactoryRetry;

public class SparqlServiceFactoryImpl
    implements SparqlServiceFactory
{
    private Map<String, QueryExecutionFactory> keyToSparqlService = new HashMap<String, QueryExecutionFactory>();
    
    private CacheFrontend cacheFrontend = null;
    
    public SparqlServiceFactoryImpl(CacheFrontend cacheFrontend) {
        this.cacheFrontend = cacheFrontend;
    }
    
    @Override
    public QueryExecutionFactory createSparqlService(String serviceUri, Collection<String> defaultGraphUris) {

        Set<String> tmp = new TreeSet<String>(defaultGraphUris);
        String key = serviceUri + tmp;
        
        QueryExecutionFactory result;
        
        result = keyToSparqlService.get(key);
        
        if(result == null) {
            
            result = new QueryExecutionFactoryHttp(serviceUri, defaultGraphUris);
            //result = new QueryExecutionFactoryPag
//            result = new QueryExecutionFactoryDelay(result, 1000l); // 1 second delay between queries
            result = new QueryExecutionFactoryRetry(result, 3, 5000l); // 3 retries, 5 second delay between retries
            if(cacheFrontend != null) {
                result = new QueryExecutionFactoryCacheEx(result, cacheFrontend);
            }
            result = new QueryExecutionFactoryPaginated(result);
            
            keyToSparqlService.put(key, result);
        }
        
        return result;
    }
    
}