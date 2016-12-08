package org.aksw.jena_sparql_api.core;

import org.apache.jena.query.QueryExecution;

public class QueryExecutionDecorator
	extends QueryExecutionDecoratorBase<QueryExecution>
{
	public QueryExecutionDecorator(QueryExecution decoratee) {
		super(decoratee);
	}
}
