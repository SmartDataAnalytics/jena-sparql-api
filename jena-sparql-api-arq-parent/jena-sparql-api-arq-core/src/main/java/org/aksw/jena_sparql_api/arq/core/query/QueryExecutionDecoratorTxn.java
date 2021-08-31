package org.aksw.jena_sparql_api.arq.core.query;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.core.Transactional;

/**
 * A query execution that starts a transaction before the actual query execution
 * and performs the commit/rollback action upon close.
 *
 * @author raven
 *
 */
public class QueryExecutionDecoratorTxn<T extends QueryExecution>
    extends QueryExecutionDecoratorBase<T>
{
    protected Transactional transactional;

    protected boolean startedTxnHere = false;
    protected Throwable seenThrowable = null;

    public QueryExecutionDecoratorTxn(T decoratee, Transactional transactional) {
        super(decoratee);
        this.transactional = transactional;
    }

    @Override
    protected void beforeExec() {
        super.beforeExec();

        if (!transactional.isInTransaction()) {
            startedTxnHere = true;
            transactional.begin(ReadWrite.READ);
        }
    }

    @Override
    protected void onException(Exception e) {
        seenThrowable = e;
        super.onException(e);
    }

    @Override
    public void close() {
        if (startedTxnHere) {
            try {
                if (seenThrowable == null) {
                    transactional.commit();
                } else {
                    transactional.abort();
                }
            } finally {
                transactional.end();
            }
        }
        super.close();
    }


    public static <T extends QueryExecution> QueryExecution wrap(T decoratee, Transactional transactional) {
        return new QueryExecutionDecoratorTxn<>(decoratee, transactional);
    }
}
