package org.aksw.jena_sparql_api.cache.core;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryWrapper;
import org.apache.jena.query.QueryExecution;

import com.google.common.cache.Cache;

public class QueryExecutionFactoryExceptionCache
	extends QueryExecutionFactoryWrapper
{
	protected Cache<String, Exception> exceptionCache;

	public QueryExecutionFactoryExceptionCache(QueryExecutionFactory decoratee, Cache<String, Exception> exceptionCache) {
		super(decoratee);
		this.exceptionCache = exceptionCache;
	}

	@Override
	protected QueryExecution wrap(QueryExecution qe) {
		QueryExecution result = new QueryExecutionExceptionCache(qe, exceptionCache);
		return result;
	}

}
