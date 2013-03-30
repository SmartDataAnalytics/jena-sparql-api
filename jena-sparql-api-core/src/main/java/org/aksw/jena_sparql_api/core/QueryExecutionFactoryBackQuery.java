package org.aksw.jena_sparql_api.core;


import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/23/11
 *         Time: 9:39 PM
 */
public abstract class QueryExecutionFactoryBackQuery
    implements QueryExecutionFactory
{
    @Override
    public QueryExecution createQueryExecution(String queryString) {
    	Query query = QueryFactory.create(queryString);
    	QueryExecution result = createQueryExecution(query);
    	return result;
    }
}
