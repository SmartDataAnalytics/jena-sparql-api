package org.aksw.jena_sparql_api.core;


import com.hp.hpl.jena.query.Query;
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
    public QueryExecutionStreaming createQueryExecution(String queryString) {
    	Query query = QueryFactory.create(queryString);
    	QueryExecutionStreaming result = createQueryExecution(query);
    	return result;
    }
}
