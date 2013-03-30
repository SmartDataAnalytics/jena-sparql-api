package org.aksw.jena_sparql_api.core;

import com.hp.hpl.jena.query.Query;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 9/20/12
 *         Time: 1:41 PM
 */
public class QueryExecutionStreamingFactory
        extends  QueryExecutionFactoryBackQuery
{
    protected QueryExecutionFactory decoratee;

    public QueryExecutionStreamingFactory(QueryExecutionFactory decoratee) {
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
}
