/**
 *
 */
package org.aksw.jena_sparql_api.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.delay.core.QueryExecutionFactoryDelay;
import org.aksw.jena_sparql_api.fallback.QueryExecutionFactoryFallback;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.limit.QueryExecutionFactoryLimit;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;
import org.aksw.jena_sparql_api.retry.core.QueryExecutionFactoryRetry;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author Lorenz Buehmann
 *
 */
public class SparqlServiceBuilder {

    private QueryExecutionFactory qef;

    public SparqlServiceBuilder(QueryExecutionFactory qef) {
        this.qef = qef;
    }

    public static SparqlServiceBuilder model(Model model){
        return new SparqlServiceBuilder(new QueryExecutionFactoryModel(model));
    }

    public static SparqlServiceBuilder http(String service, String ... defaultGraphs){
        return http(service, Arrays.asList(defaultGraphs));
    }

    public static SparqlServiceBuilder http(String service, Collection<String> defaultGraphs){
        return new SparqlServiceBuilder(new QueryExecutionFactoryHttp(service, defaultGraphs));
    }

    public static SparqlServiceBuilder http(SparqlServiceReference sparqlService){
        return http(sparqlService.getServiceURL(), sparqlService.getDefaultGraphURIs());
    }

    public static SparqlServiceBuilder http(Collection<SparqlServiceReference> sparqlServices){
        if(sparqlServices.size() == 1){
            return http(sparqlServices.iterator().next());
        }
        List<QueryExecutionFactory> decoratees = new ArrayList<QueryExecutionFactory>(sparqlServices.size());
        for (SparqlServiceReference sparqlService : sparqlServices) {
            decoratees.add(new QueryExecutionFactoryHttp(sparqlService.getServiceURL(), sparqlService.getDefaultGraphURIs()));
        }
        return new SparqlServiceBuilder(new QueryExecutionFactoryFallback(decoratees));
    }

    public SparqlServiceBuilder withDelay(long delayDuration, TimeUnit delayTimeUnit){
        qef = new QueryExecutionFactoryDelay(qef, delayDuration, delayTimeUnit);
        return this;
    }

    public SparqlServiceBuilder withPagination(long pageSize){
        qef = new QueryExecutionFactoryPaginated(qef, pageSize);
        return this;
    }

    public SparqlServiceBuilder withRetry(int retryCount, long retryDelayDuration, TimeUnit retryDelayTimeUnit){
        qef = new QueryExecutionFactoryRetry(qef, retryCount, retryDelayDuration, retryDelayTimeUnit);
        return this;
    }

    public SparqlServiceBuilder withCache(CacheFrontend cache){
        qef = new QueryExecutionFactoryCacheEx(qef, cache);
        return this;
    }

    public SparqlServiceBuilder withDefaultLimit(long limit, boolean doCloneQuery){
        qef = new QueryExecutionFactoryLimit(qef, doCloneQuery, limit);
        return this;
    }

    /**
     * Return the final query execution factory.
     * @return
     */
    public QueryExecutionFactory create(){
        return qef;
    }

}
