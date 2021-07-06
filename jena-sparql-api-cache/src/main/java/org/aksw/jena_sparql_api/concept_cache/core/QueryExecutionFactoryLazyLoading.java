package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.function.Function;

import org.aksw.commons.rx.range.RangedSupplier;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDecorator;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.engine.binding.Binding;

public class QueryExecutionFactoryLazyLoading
	extends QueryExecutionFactoryDecorator
{
	protected Function<Query, RangedSupplier<Long, Binding>> queryToRangedSupplier;
	
	public QueryExecutionFactoryLazyLoading(QueryExecutionFactory decoratee, Function<Query, RangedSupplier<Long, Binding>> queryToRangedSupplier) {
		super(decoratee);

		this.queryToRangedSupplier = queryToRangedSupplier;
	}
	
	@Override
	public QueryExecution createQueryExecution(Query query) {
		
		// Perform a lookup for the 
		//LazyLoadingCachingList<Op>
		
		
		// TODO Auto-generated method stub
		return super.createQueryExecution(query);
	}
}
