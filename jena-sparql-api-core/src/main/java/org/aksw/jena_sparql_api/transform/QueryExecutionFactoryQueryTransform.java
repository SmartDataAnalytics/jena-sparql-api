package org.aksw.jena_sparql_api.transform;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDecorator;

import com.google.common.base.Function;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;

public class QueryExecutionFactoryQueryTransform
	extends QueryExecutionFactoryDecorator
{
	protected Function<Query, Query> transform;

	public QueryExecutionFactoryQueryTransform(QueryExecutionFactory decoratee, Function<Query, Query> transform) {
		super(decoratee);
		this.transform = transform;
	}

	@Override
	public QueryExecution createQueryExecution(String queryString) {
		throw new RuntimeException("Query must be parsed");
	}

	@Override
	public QueryExecution createQueryExecution(Query query) {
		Query tmp = transform.apply(query);
		QueryExecution result = super.createQueryExecution(tmp);
		return result;
	}
}
