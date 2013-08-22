package org.aksw.jena_sparql_api.retry.core;


import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDecorator;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;

public class QueryExecutionFactoryRetry
	extends QueryExecutionFactoryDecorator
{
	private int retryCount;
	private long retryDelayInMs;
	
	public QueryExecutionFactoryRetry(QueryExecutionFactory decoratee, int retryCount, long retryDelayInMs) {
		super(decoratee);
		this.retryCount = retryCount;
		this.retryDelayInMs = retryDelayInMs;
	}

	@Override
	public QueryExecution createQueryExecution(Query query) {
		QueryExecution qe = super.createQueryExecution(query);
		QueryExecution result = new QueryExecutionRetry(qe, retryCount, retryDelayInMs);
		return result;
	}

	@Override
	public QueryExecution createQueryExecution(String queryString) {
		QueryExecution qe = super.createQueryExecution(queryString);
		QueryExecution result = new QueryExecutionRetry(qe, retryCount, retryDelayInMs);
		return result;
	}

}