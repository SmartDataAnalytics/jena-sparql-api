package org.aksw.jena_sparql_api.core;

import java.util.HashMap;
import java.util.Map;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;
import org.aksw.jena_sparql_api.utils.DatasetDescriptionUtils;
import org.apache.http.client.HttpClient;
import org.apache.jena.sparql.core.DatasetDescription;


public class SparqlServiceFactoryImpl
    implements SparqlServiceFactory
{
    private Map<String, SparqlService> keyToSparqlService = new HashMap<String, SparqlService>();

    private CacheFrontend cacheFrontend;
    private SparqlServiceFactory delegate;
    private Integer pageSize;;

    public SparqlServiceFactoryImpl(CacheFrontend cacheFrontend) {
        this(new SparqlServiceFactoryHttp(), cacheFrontend, null);
    }

    public SparqlServiceFactoryImpl(CacheFrontend cacheFrontend, Integer pageSize) {
        this(new SparqlServiceFactoryHttp(), cacheFrontend, pageSize);
    }

    public SparqlServiceFactoryImpl(Integer pageSize) {
        this(new SparqlServiceFactoryHttp(), null, pageSize);
    }

    public SparqlServiceFactoryImpl(SparqlServiceFactory delegate, CacheFrontend cacheFrontend, Integer pageSize) {
        this.delegate = delegate;
        this.cacheFrontend = cacheFrontend;
        this.pageSize = pageSize;
    }

    @Override
    public SparqlService createSparqlService(String serviceUri, DatasetDescription datasetDescription, HttpClient httpClient) {

    	if(datasetDescription == null) {
    		datasetDescription = new DatasetDescription();
    	}

        String str = DatasetDescriptionUtils.toString(datasetDescription);
        String key = serviceUri + str;

        SparqlService result;

        result = keyToSparqlService.get(key);

        if(result == null) {

            SparqlService tmp = delegate.createSparqlService(serviceUri, datasetDescription, httpClient);

            QueryExecutionFactory qef = tmp.getQueryExecutionFactory();

            if(cacheFrontend != null) {
                qef = new QueryExecutionFactoryCacheEx(qef, cacheFrontend);
            }

            if(pageSize != null && pageSize >= 0) {
                qef = new QueryExecutionFactoryPaginated(qef);
            }

            result = new SparqlServiceImpl(qef, tmp.getUpdateExecutionFactory());

            keyToSparqlService.put(key, result);
        }

        return result;
    }
}
