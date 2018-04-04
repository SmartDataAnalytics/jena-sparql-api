package org.aksw.jena_sparql_api.fail;

import org.aksw.jena_sparql_api.core.QueryExecutionBaseSelect;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

public class QueryExecutionAlwaysFail
	extends QueryExecutionBaseSelect
{
	protected String queryString;

	public QueryExecutionAlwaysFail(String queryString) {
		super(null, null);
		this.queryString = queryString;
	}
	
	public QueryExecutionAlwaysFail(Query query) {
		super(query, null);
		this.queryString = "" + query;
	}

	@Override
	protected QueryExecution executeCoreSelectX(Query query) {
		throw new RuntimeException("Encountered request to execute a query on always failing query execution object");
	}
}
