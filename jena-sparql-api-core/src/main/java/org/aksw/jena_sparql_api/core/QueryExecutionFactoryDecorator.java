package org.aksw.jena_sparql_api.core;

import com.hp.hpl.jena.query.Query;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 12:53 PM
 */
public class QueryExecutionFactoryDecorator
    implements QueryExecutionFactory
{
    private QueryExecutionFactory decoratee;

    public QueryExecutionFactoryDecorator(QueryExecutionFactory decoratee) {
        this.decoratee = decoratee;
    }

    @Override
    public String getId() {
        return decoratee.getId();
    }

    @Override
    public String getState() {
        return decoratee.getState();
    }

    @Override
    public QueryExecutionStreaming createQueryExecution(Query query) {
        return decoratee.createQueryExecution(query);
    }

    @Override
    public QueryExecutionStreaming createQueryExecution(String queryString) {
        return decoratee.createQueryExecution(queryString);
    }
}
