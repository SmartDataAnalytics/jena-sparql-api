package org.aksw.jena_sparql_api.core;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.delay.core.QueryExecutionFactoryDelay;
import org.aksw.jena_sparql_api.limit.QueryExecutionFactoryLimit;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;
import org.aksw.jena_sparql_api.parse.QueryExecutionFactoryParse;
import org.aksw.jena_sparql_api.post_process.QueryExecutionFactoryPostProcess;
import org.aksw.jena_sparql_api.prefix.core.QueryExecutionFactoryPrefix;
import org.aksw.jena_sparql_api.retry.core.QueryExecutionFactoryRetry;
import org.aksw.jena_sparql_api.transform.QueryExecutionFactoryQueryTransform;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.DatasetDescription;

import com.google.common.base.Supplier;

/**
 * A fluent API for conveniently building 'recipes' of transformations to apply to any QueryExecutionFactory.
 *
 * Custom functions can be chained using the generic compose(yourFn) method.
 *
 * @author raven
 *
 * @param <T>
 */
public class FluentQueryExecutionFactoryFn<P>
    extends FluentFnBase<QueryExecutionFactory, P>
{
    public FluentQueryExecutionFactoryFn() {
    }

    @Override
    public FluentQueryExecutionFactoryFn<P> compose(Function<QueryExecutionFactory, QueryExecutionFactory> nextFn) {
        super.compose(nextFn);
        return this;
    }


    public FluentQueryExecutionFactoryFn(Supplier<P> parentSupplier, Function<QueryExecutionFactory, QueryExecutionFactory> fn) {
        this.parentSupplier = parentSupplier;
        this.fn = fn;
    }

    public FluentQueryExecutionFactoryFn<P> withDelay(final int delayDuration, final TimeUnit delayTimeUnit) {
        compose(new Function<QueryExecutionFactory, QueryExecutionFactory>() {
            @Override
            public QueryExecutionFactory apply(QueryExecutionFactory qef) {
                QueryExecutionFactory r = new QueryExecutionFactoryDelay(qef, delayDuration, delayTimeUnit);
                return r;
            }
        });

        return this;
    }

    public FluentQueryExecutionFactoryFn<P> withPrefixes(final PrefixMapping pm, final boolean doClone) {
        /**
         * TODO: Convert to use of a Query transformation (Query -> Query)
         */
        compose(new Function<QueryExecutionFactory, QueryExecutionFactory>() {
            @Override
            public QueryExecutionFactory apply(QueryExecutionFactory qef) {
                QueryExecutionFactory r = new QueryExecutionFactoryPrefix(qef, pm, doClone);
                return r;
            }
        });

        return this;
    }

    public FluentQueryExecutionFactoryFn<P> withParser(final Function<String, Query> parser) {
        compose(new Function<QueryExecutionFactory, QueryExecutionFactory>() {
            @Override
            public QueryExecutionFactory apply(QueryExecutionFactory qef) {
                QueryExecutionFactory r = new QueryExecutionFactoryParse(qef, parser);
                return r;
            }
        });

        return this;
    }

    public FluentQueryExecutionFactoryFn<P> withPagination(final int pageSize) {
        compose(new Function<QueryExecutionFactory, QueryExecutionFactory>() {
            @Override
            public QueryExecutionFactory apply(QueryExecutionFactory qef) {
                QueryExecutionFactory r = new QueryExecutionFactoryPaginated(qef, pageSize);
                return r;
            }
        });

        return this;
    }

    public FluentQueryExecutionFactoryFn<P> withRetry(final int retryCount, final long retryDelayDuration, final TimeUnit retryDelayTimeUnit) {
        compose(new Function<QueryExecutionFactory, QueryExecutionFactory>() {
            @Override
            public QueryExecutionFactory apply(QueryExecutionFactory qef) {
                QueryExecutionFactory r = new QueryExecutionFactoryRetry(qef, retryCount, retryDelayDuration, retryDelayTimeUnit);
                return r;
            }
        });

        return this;
    }

    public FluentQueryExecutionFactoryFn<P> withCache(final CacheFrontend cache){
        compose(new Function<QueryExecutionFactory, QueryExecutionFactory>() {
            @Override
            public QueryExecutionFactory apply(QueryExecutionFactory qef) {
                QueryExecutionFactory r = qef = new QueryExecutionFactoryCacheEx(qef, cache);
                return r;
            }
        });

        return this;
    }

    public FluentQueryExecutionFactoryFn<P> withDefaultLimit(final long limit, final boolean doCloneQuery){
        compose(new Function<QueryExecutionFactory, QueryExecutionFactory>() {
            @Override
            public QueryExecutionFactory apply(QueryExecutionFactory qef) {
                QueryExecutionFactory r = new QueryExecutionFactoryLimit(qef, doCloneQuery, limit);
                return r;
            }
        });

        return this;
    }

    public FluentQueryExecutionFactoryFn<P> withQueryTransform(final Function<Query, Query> queryTransform){
        compose(new Function<QueryExecutionFactory, QueryExecutionFactory>() {
            @Override
            public QueryExecutionFactory apply(QueryExecutionFactory qef) {
                QueryExecutionFactory r = new QueryExecutionFactoryQueryTransform(qef, queryTransform);
                return r;
            }
        });

        return this;
    }

    /**
     * Configure a function for post processing QueryExecution instances before returning them to the application.
     *
     * Note: consumer is probably not the semantically appropriate interface
     *
     * @param postProcessor
     * @return
     */
    public FluentQueryExecutionFactoryFn<P> withPostProcessor(final Consumer<QueryExecution> postProcessor) {
        compose(new Function<QueryExecutionFactory, QueryExecutionFactory>() {
            @Override
            public QueryExecutionFactory apply(QueryExecutionFactory qef) {
                QueryExecutionFactory r = new QueryExecutionFactoryPostProcess(qef, postProcessor);
                return r;
            }
        });

        return this;
    }


    public FluentQueryExecutionFactoryFn<P> withDatasetDescription(final DatasetDescription datasetDescription) {
        compose(new Function<QueryExecutionFactory, QueryExecutionFactory>() {
            @Override
            public QueryExecutionFactory apply(QueryExecutionFactory qef) {
                QueryExecutionFactory r = new QueryExecutionFactoryDatasetDescription(qef, datasetDescription);
                return r;
            }
        });

        return this;
    }

    /**
     * Transforms all non-SELECT query forms to SELECT queries.
     * I.e. rewrites ASK, DESCRIBE and CONSTRUCT to SELECT queries.
     * Only the select query is passed on for further execution, whereas the
     * final response is constructed from the result set.
     *
     * @return
     */
    public FluentQueryExecutionFactoryFn<P> selectOnly() {
        compose(new Function<QueryExecutionFactory, QueryExecutionFactory>() {
            @Override
            public QueryExecutionFactory apply(QueryExecutionFactory qef) {
                QueryExecutionFactory r = new QueryExecutionFactorySelect(qef);
                return r;
            }
        });

        return this;
    }

    public static FluentQueryExecutionFactoryFn<?> start() {
        return new FluentQueryExecutionFactoryFn<Object>();
    }

}

