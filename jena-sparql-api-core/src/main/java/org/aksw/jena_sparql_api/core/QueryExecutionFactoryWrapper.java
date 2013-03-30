package org.aksw.jena_sparql_api.core;

import com.hp.hpl.jena.query.Query;

/**
 * Similar to QueryExecutionFactoryDecorator, except that a postProcess method can
 * be overriden to do something with the QueryExecution object
 * 
 * @author raven
 *
 */
public abstract class QueryExecutionFactoryWrapper
	extends QueryExecutionFactoryDecorator
{

	public QueryExecutionFactoryWrapper(QueryExecutionFactory decoratee) {
		super(decoratee);
	}

	protected abstract QueryExecutionStreaming wrap(QueryExecutionStreaming qe);
	
    @Override
    public QueryExecutionStreaming createQueryExecution(Query query) {
    	QueryExecutionStreaming tmp = super.createQueryExecution(query);

    	QueryExecutionStreaming result = wrap(tmp); 
    	
    	return result;
    }

    @Override
    public QueryExecutionStreaming createQueryExecution(String queryString) {
    	QueryExecutionStreaming tmp = super.createQueryExecution(queryString);
    	
    	QueryExecutionStreaming result = wrap(tmp); 
    	
    	return result;    	
    }
}