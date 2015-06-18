package org.aksw.jena_sparql_api.core;

import java.util.HashMap;
import java.util.Map;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;
import org.aksw.jena_sparql_api.utils.DatasetDescriptionUtils;

import com.hp.hpl.jena.sparql.core.DatasetDescription;


public class SparqlServiceFactoryImpl
    implements SparqlServiceFactory
{
    private Map<String, QueryExecutionFactory> keyToSparqlService = new HashMap<String, QueryExecutionFactory>();

    private CacheFrontend cacheFrontend = null;
    private SparqlServiceFactory delegate;

    public SparqlServiceFactoryImpl(CacheFrontend cacheFrontend) {
        this(new SparqlServiceFactoryHttp(), cacheFrontend);
    }

    public SparqlServiceFactoryImpl(SparqlServiceFactory delegate, CacheFrontend cacheFrontend) {
        this.delegate = delegate;
        this.cacheFrontend = cacheFrontend;
    }

    @Override
    public QueryExecutionFactory createSparqlService(String serviceUri, DatasetDescription datasetDescription, Object authenticator) {

        String tmp = DatasetDescriptionUtils.toString(datasetDescription);
        String key = serviceUri + tmp;

        QueryExecutionFactory result;

        result = keyToSparqlService.get(key);

        if(result == null) {

            result = delegate.createSparqlService(serviceUri, datasetDescription, authenticator);

            if(cacheFrontend != null) {
                result = new QueryExecutionFactoryCacheEx(result, cacheFrontend);
            }
            result = new QueryExecutionFactoryPaginated(result);

            keyToSparqlService.put(key, result);
        }

        return result;
    }
}
