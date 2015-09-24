package org.aksw.jena_sparql_api.prefix.core;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDecorator;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.shared.PrefixMapping;

public class QueryExecutionFactoryPrefix
	extends QueryExecutionFactoryDecorator
{
	private PrefixMapping prefixMapping;
	boolean doClone;
	
	public QueryExecutionFactoryPrefix(QueryExecutionFactory decoratee, PrefixMapping prefixMapping, boolean doClone) {
		super(decoratee);
		this.prefixMapping = prefixMapping;
		this.doClone = doClone;
	}

	@Override
	public QueryExecution createQueryExecution(Query query) {
		Query q = doClone ? query.cloneQuery() : query;
		
		q.getPrefixMapping().setNsPrefixes(prefixMapping);

		QueryExecution result = super.createQueryExecution(q);
		return result;
	}

	@Override
	public QueryExecution createQueryExecution(String queryString) {
		throw new RuntimeException("query object required");
	}
}
