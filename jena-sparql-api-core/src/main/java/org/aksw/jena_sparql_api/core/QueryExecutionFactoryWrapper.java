package org.aksw.jena_sparql_api.core;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

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

	protected abstract QueryExecution wrap(QueryExecution qe);
	
    @Override
    public QueryExecution createQueryExecution(Query query) {
    	QueryExecution tmp = super.createQueryExecution(query);

    	QueryExecution result = wrap(tmp); 
    	
    	return result;
    }

    @Override
    public QueryExecution createQueryExecution(String queryString) {
    	QueryExecution tmp = super.createQueryExecution(queryString);
    	
    	QueryExecution result = wrap(tmp); 
    	
    	return result;    	
    }
}