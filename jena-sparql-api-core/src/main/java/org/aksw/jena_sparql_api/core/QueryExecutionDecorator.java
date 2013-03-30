package org.aksw.jena_sparql_api.core;

import com.hp.hpl.jena.query.QueryExecution;

public class QueryExecutionDecorator
	extends QueryExecutionDecoratorBase<QueryExecution>
{
	public QueryExecutionDecorator(QueryExecution decoratee) {
		super(decoratee);
	}
}
