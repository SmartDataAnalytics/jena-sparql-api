package org.aksw.jena_sparql_api.core;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheBackend;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontendImpl;
import org.aksw.jena_sparql_api.delay.core.QueryExecutionFactoryDelay;
import org.aksw.jena_sparql_api.delay.extra.Delayer;
import org.aksw.jena_sparql_api.limit.QueryExecutionFactoryLimit;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;
import org.aksw.jena_sparql_api.parse.QueryExecutionFactoryParse;
import org.aksw.jena_sparql_api.post_process.QueryExecutionFactoryPostProcess;
import org.aksw.jena_sparql_api.prefix.core.QueryExecutionFactoryPrefix;
import org.aksw.jena_sparql_api.retry.core.QueryExecutionFactoryRetry;
import org.aksw.jena_sparql_api.timeout.QueryExecutionTimeoutExogeneous;
import org.aksw.jena_sparql_api.transform.QueryExecutionFactoryQueryTransform;
import org.aksw.jena_sparql_api.transform.result_set.QueryExecutionFactoryTransformResult;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.graph.NodeTransform;

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
        this(null, null);
    }

    @Override
    public FluentQueryExecutionFactoryFn<P> compose(Function<QueryExecutionFactory, QueryExecutionFactory> nextFn) {
        super.compose(nextFn);
        return this;
    }


    public FluentQueryExecutionFactoryFn(Supplier<P> parentSupplier, Function<QueryExecutionFactory, QueryExecutionFactory> fn) {
        super(true);
        this.parentSupplier = parentSupplier;
        this.fn = fn;
    }

//    public FluentQueryExecutionFactoryFn<P> withDelayFromNow(final int delayDuration, final TimeUnit delayTimeUnit) {
//    	Delayer delayer = DelayerDefault.createFromNow(delay)
//    	delayTimeUnit.toMillis(delayDuration)
//    	
//        compose(qef -> new QueryExecutionFactoryDelay(qef, delayDuration, delayTimeUnit));
//    }

    public FluentQueryExecutionFactoryFn<P> withDelay(final int delayDuration, final TimeUnit delayTimeUnit) {
        compose(qef -> new QueryExecutionFactoryDelay(qef, delayDuration, delayTimeUnit));

//    	compose(new Function<QueryExecutionFactory, QueryExecutionFactory>() {
//            @Override
//            public QueryExecutionFactory apply(QueryExecutionFactory qef) {
//                QueryExecutionFactory r = new QueryExecutionFactoryDelay(qef, delayDuration, delayTimeUnit);
//                return r;
//            }
//        });

        return this;
    }

    
    public FluentQueryExecutionFactoryFn<P> withDelay(final Delayer delayer) {
        compose(qef -> new QueryExecutionFactoryDelay(qef, delayer));

        return this;
    }

    public FluentQueryExecutionFactoryFn<P> withPrefixes(final PrefixMapping pm, final boolean doClone) {
    	compose(qef -> new QueryExecutionFactoryPrefix(qef, pm, doClone));
    	/**
         * TODO: Convert to use of a Query transformation (Query -> Query)
         */
//        compose(new Function<QueryExecutionFactory, QueryExecutionFactory>() {
//            @Override
//            public QueryExecutionFactory apply(QueryExecutionFactory qef) {
//                QueryExecutionFactory r = new QueryExecutionFactoryPrefix(qef, pm, doClone);
//                return r;
//            }
//        });

        return this;
    }

    public FluentQueryExecutionFactoryFn<P> withParser(final Function<String, Query> parser) {
    	compose(qef -> new QueryExecutionFactoryParse(qef, parser));

//        compose(new Function<QueryExecutionFactory, QueryExecutionFactory>() {
//            @Override
//            public QueryExecutionFactory apply(QueryExecutionFactory qef) {
//                QueryExecutionFactory r = new QueryExecutionFactoryParse(qef, parser);
//                return r;
//            }
//        });

        return this;

    }

    
    public FluentQueryExecutionFactoryFn<P> withClientSideConstruct() {
        compose(new Function<QueryExecutionFactory, QueryExecutionFactory>() {
            @Override
            public QueryExecutionFactory apply(QueryExecutionFactory qef) {
                QueryExecutionFactory r = new QueryExecutionFactorySelect(qef);
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
    	compose(qef -> new QueryExecutionFactoryRetry(qef, retryCount, retryDelayDuration, retryDelayTimeUnit));
// compose(new Function<QueryExecutionFactory, QueryExecutionFactory>() {
//            @Override
//            public QueryExecutionFactory apply(QueryExecutionFactory qef) {
//                QueryExecutionFactory r = new QueryExecutionFactoryRetry(qef, retryCount, retryDelayDuration, retryDelayTimeUnit);
//                return r;
//            }
//        });

        return this;
    }

    public FluentQueryExecutionFactoryFn<P> withCache(final CacheBackend cache){
    	return withCache(new CacheFrontendImpl(cache));
    }
    
    public FluentQueryExecutionFactoryFn<P> withCache(final CacheFrontend cache){
    	compose(qef -> new QueryExecutionFactoryCacheEx(qef, cache));

//        compose(new Function<QueryExecutionFactory, QueryExecutionFactory>() {
//            @Override
//            public QueryExecutionFactory apply(QueryExecutionFactory qef) {
//                QueryExecutionFactory r = qef = new QueryExecutionFactoryCacheEx(qef, cache);
//                return r;
//            }
//        });

        return this;
    }

    /**
     * Transform nodes of SPARQL results (models, datasets, result sets).
     * Can be used for e.g. for blank node skolemization.
     * 
     * @param nodeTransform
     * @return
     */
    public FluentQueryExecutionFactoryFn<P> withResultTransform(final NodeTransform nodeTransform) {
        compose(qef -> new QueryExecutionFactoryTransformResult(qef, nodeTransform));

        return this;
    }

    public FluentQueryExecutionFactoryFn<P> withDefaultLimit(final long limit, final boolean doCloneQuery){
    	compose(qef -> new QueryExecutionFactoryLimit(qef, doCloneQuery, limit));

//    	compose(new Function<QueryExecutionFactory, QueryExecutionFactory>() {
//            @Override
//            public QueryExecutionFactory apply(QueryExecutionFactory qef) {
//                QueryExecutionFactory r = new QueryExecutionFactoryLimit(qef, doCloneQuery, limit);
//                return r;
//            }
//        });

        return this;
    }

    public FluentQueryExecutionFactoryFn<P> withQueryTransform(final Function<? super Query, ? extends Query> queryTransform){
    	compose(qef -> new QueryExecutionFactoryQueryTransform(qef, queryTransform));

//        compose(new Function<QueryExecutionFactory, QueryExecutionFactory>() {
//            @Override
//            public QueryExecutionFactory apply(QueryExecutionFactory qef) {
//                QueryExecutionFactory r = new QueryExecutionFactoryQueryTransform(qef, queryTransform);
//                return r;
//            }
//        });

        return this;
    }

    /**
     * Given a consumer of 'x', return a function that
     * first invokes the consumer and then yields 'x'.
     * (i.e. return an identity function with the consumer's side effect)
     * 
     * @param consumer
     * @return
     */
    public static <T> Function<T, T> toFunction(Consumer<T> consumer) {
    	return x -> { consumer.accept(x); return x; };
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
        withPostTransformer(toFunction(postProcessor));
        return this;
    }

    public FluentQueryExecutionFactoryFn<P> withPostTransformer(final Function<? super QueryExecution, ? extends QueryExecution> postProcessor) {
        compose(qef -> new QueryExecutionFactoryPostProcess(qef, postProcessor));
        
        return this;
    }

	public FluentQueryExecutionFactoryFn<P> withExogeneousTimeout() {
		withPostTransformer(qe -> {
			QueryExecution r = new QueryExecutionTimeoutExogeneous(qe);
			return r;
		});
	
		return this;
	}

//	public FluentQueryExecutionFactoryFn<P> withExogeneousTimeout(long millis) {
//		withPostProcessor(qe -> {
//			QueryExecution r = new QueryExecutionTimeoutExogeneous(qe);
//			r.setTimeout(millis);
//			return r;
//		});
////		compose(new Function<QueryExecutionFactory, QueryExecutionFactory>() {
////			@Override
////			public QueryExecutionFactory apply(QueryExecutionFactory qef) {
////				QueryExecutionFactory r = new QueryExecutionTimeoutExogeneous(qef, postProcessor);
////				return r;
////			}
////		});
//
//		return this;
//	}

//    public FluentQueryExecutionFactoryFn<P> withTimeoutHandler(final Consumer<SparqlStmtQuery> callback) {
//        compose(new Function<QueryExecutionFactory, QueryExecutionFactory>() {
//            @Override
//            public QueryExecutionFactory apply(QueryExecutionFactory qef) {
//                QueryExecutionFactory r = new QueryExecutionFactoryPostProcess(qef, postProcessor);
//                return r;
//            }
//        });
//
//        return this;
//    }


    public FluentQueryExecutionFactoryFn<P> withDatasetDescription(final DatasetDescription datasetDescription) {
    	compose(qef -> new QueryExecutionFactoryDatasetDescription(qef, datasetDescription));
//        compose(new Function<QueryExecutionFactory, QueryExecutionFactory>() {
//            @Override
//            public QueryExecutionFactory apply(QueryExecutionFactory qef) {
//                QueryExecutionFactory r = new QueryExecutionFactoryDatasetDescription(qef, datasetDescription);
//                return r;
//            }
//        });

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
    	compose(QueryExecutionFactorySelect::new);

//        compose(new Function<QueryExecutionFactory, QueryExecutionFactory>() {
//            @Override
//            public QueryExecutionFactory apply(QueryExecutionFactory qef) {
//                QueryExecutionFactory r = new QueryExecutionFactorySelect(qef);
//                return r;
//            }
//        });
//
        return this;
    }

    public static FluentQueryExecutionFactoryFn<?> start() {
        return new FluentQueryExecutionFactoryFn<Object>();
    }

}

