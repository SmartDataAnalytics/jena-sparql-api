package org.aksw.jena_sparql_api.core;

import org.apache.jena.query.QueryExecution;

/**
 * QueryExecution for before and after exec actions
 *
 * @author raven
 *
 */
public class QueryExecutionExecWrapper
    extends QueryExecutionDecorator
{
    protected Runnable beforeExecAction;
    protected Runnable afterExecAction;

    public QueryExecutionExecWrapper(QueryExecution decoratee, Runnable beforeExecAction, Runnable afterExecAction) {
        super(decoratee);
        this.beforeExecAction = beforeExecAction;
        this.afterExecAction = afterExecAction;
    }

    @Override
    protected void beforeExec() {
        super.beforeExec(); // Should not do anything, but left here just in case
        beforeExecAction.run();
    }

    @Override
    protected void afterExec() {
        afterExecAction.run();
        super.afterExec(); // Should not do anything, but left here just in case
    }

}
