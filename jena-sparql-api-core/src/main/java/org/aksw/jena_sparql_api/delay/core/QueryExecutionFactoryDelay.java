package org.aksw.jena_sparql_api.delay.core;


import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDecorator;
import org.aksw.jena_sparql_api.core.QueryExecutionStreaming;
import org.aksw.jena_sparql_api.delay.extra.Delayer;
import org.aksw.jena_sparql_api.delay.extra.DelayerDefault;

import com.hp.hpl.jena.query.Query;

/**
 * A query execution factory, which generates query executions
 * that delay execution
 *
 *
 * @author Claus Stadler
 *
 *
 *         Date: 7/26/11
 *         Time: 10:27 AM
 */
public class QueryExecutionFactoryDelay
        extends QueryExecutionFactoryDecorator {
    private Delayer delayer;

    public QueryExecutionFactoryDelay(QueryExecutionFactory decoratee) {
        this(decoratee, 1000);
    }

    public QueryExecutionFactoryDelay(QueryExecutionFactory decoratee, long delay) {
        this(decoratee, new DelayerDefault(delay));
    }

    public QueryExecutionFactoryDelay(QueryExecutionFactory decoratee, Delayer delayer) {
        super(decoratee);
        this.delayer = delayer;
    }

    @Override
    public QueryExecutionStreaming createQueryExecution(Query query) {
        return new QueryExecutionDelay(super.createQueryExecution(query), delayer);
    }

    @Override
    public QueryExecutionStreaming createQueryExecution(String queryString) {
        return new QueryExecutionDelay(super.createQueryExecution(queryString), delayer);
    }
}
