/**
 *
 */
package org.aksw.jena_sparql_api.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.aksw.jena_sparql_api.fallback.QueryExecutionFactoryFallback;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.util.Context;

import com.google.common.base.Function;
import com.google.common.base.Supplier;

/**
 * @author Lorenz Buehmann
 *
 */
public class FluentQueryExecutionFactory<P>
    extends FluentBase<QueryExecutionFactory, P>
{

    //private QueryExecutionFactory qef;

    public FluentQueryExecutionFactory(QueryExecutionFactory qef) {
        this.fn = qef;
    }

    public static FluentQueryExecutionFactory<?> model(Model model) {
        return new FluentQueryExecutionFactory<Object>(new QueryExecutionFactoryModel(model));
    }

    public static FluentQueryExecutionFactory<?> dataset(Dataset dataset) {
        return new FluentQueryExecutionFactory<Object>(new QueryExecutionFactoryDataset(dataset));
    }

    public static FluentQueryExecutionFactory<?> dataset(Dataset dataset, Context context) {
        return new FluentQueryExecutionFactory<Object>(new QueryExecutionFactoryDataset(dataset, context));
    }


    public static FluentQueryExecutionFactory<?> from(Dataset dataset) {
        return new FluentQueryExecutionFactory<Object>(new QueryExecutionFactoryDataset(dataset));
    }

    public static FluentQueryExecutionFactory<?> from(DatasetGraph datasetGraph) {
        return new FluentQueryExecutionFactory<Object>(new QueryExecutionFactoryDatasetGraph(datasetGraph, false));
    }

    public static FluentQueryExecutionFactory<?> defaultDatasetGraph() {
        return FluentQueryExecutionFactory.start(DatasetGraphFactory.createMem());
    }

    public static FluentQueryExecutionFactory<?> start(DatasetGraph datasetGraph){
        return new FluentQueryExecutionFactory<Object>(new QueryExecutionFactoryDatasetGraph(datasetGraph, false));
    }

    public static FluentQueryExecutionFactory<?> http(String service, String ... defaultGraphs){
        return http(service, Arrays.asList(defaultGraphs));
    }

    public static FluentQueryExecutionFactory<?> http(String service, Collection<String> defaultGraphs){
        return new FluentQueryExecutionFactory<Object>(new QueryExecutionFactoryHttp(service, defaultGraphs));
    }

    public static FluentQueryExecutionFactory<?> http(SparqlServiceReference sparqlService){
        return http(sparqlService.getServiceURL(), sparqlService.getDefaultGraphURIs());
    }

    public static FluentQueryExecutionFactory<?> http(Collection<SparqlServiceReference> sparqlServices){
        if(sparqlServices.size() == 1){
            return http(sparqlServices.iterator().next());
        }
        List<QueryExecutionFactory> decoratees = new ArrayList<QueryExecutionFactory>(sparqlServices.size());
        for (SparqlServiceReference sparqlService : sparqlServices) {
            decoratees.add(new QueryExecutionFactoryHttp(sparqlService.getServiceURL(), sparqlService.getDefaultGraphURIs()));
        }
        return new FluentQueryExecutionFactory<Object>(new QueryExecutionFactoryFallback(decoratees));
    }



    public FluentQueryExecutionFactoryFn<FluentQueryExecutionFactory<P>> config() {
        final FluentQueryExecutionFactory<P> self = this;

        final FluentQueryExecutionFactoryFn<FluentQueryExecutionFactory<P>> result = new FluentQueryExecutionFactoryFn<FluentQueryExecutionFactory<P>>();
        result.setParentSupplier(new Supplier<FluentQueryExecutionFactory<P>>() {
                @Override
                public FluentQueryExecutionFactory<P> get() {
                    // Apply the collection transformations
                    Function<QueryExecutionFactory, QueryExecutionFactory> transform = result.value();
                    fn = transform.apply(fn);

                    return self;
                }
            });

        return result;
    }



/*

    public FluentQueryExecutionFactory<T> withDelay(long delayDuration, TimeUnit delayTimeUnit){
        qef = new QueryExecutionFactoryDelay(qef, delayDuration, delayTimeUnit);
        return this;
    }

    public FluentQueryExecutionFactory<T> withPagination(long pageSize){
        qef = new QueryExecutionFactoryPaginated(qef, pageSize);
        return this;
    }

    public FluentQueryExecutionFactory<T> withRetry(int retryCount, long retryDelayDuration, TimeUnit retryDelayTimeUnit){
        qef = new QueryExecutionFactoryRetry(qef, retryCount, retryDelayDuration, retryDelayTimeUnit);
        return this;
    }

    public FluentQueryExecutionFactory<T> withCache(CacheFrontend cache){
        qef = new QueryExecutionFactoryCacheEx(qef, cache);
        return this;
    }

    public FluentQueryExecutionFactory<T> withDefaultLimit(long limit, boolean doCloneQuery){
        qef = new QueryExecutionFactoryLimit(qef, doCloneQuery, limit);
        return this;
    }
*/
    /**
     * Return the final query execution factory.
     * @return
     */
//    public QueryExecutionFactory create() {
//        return qef;
//    }
//
//    public T end() {
//        throw new RuntimeException("A call to .end() is invalid here");
//    }
}
