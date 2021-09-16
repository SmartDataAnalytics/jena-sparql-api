package org.aksw.jena_sparql_api.arq.core.update;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.update.UpdateProcessor;

public class UpdateProcessorDecoratorTxn<T extends UpdateProcessor>
    extends UpdateProcessorDecoratorBase<T>
{
    protected Transactional transactional;

    protected boolean startedTxnHere = false;
    protected Throwable seenThrowable = null;

    public UpdateProcessorDecoratorTxn(T decoratee, Transactional transactional) {
        super(decoratee);
        this.transactional = transactional;
    }

    @Override
    protected void beforeExec() {
        super.beforeExec();

        if (!transactional.isInTransaction()) {
            startedTxnHere = true;
            transactional.begin(ReadWrite.WRITE);
        }
    }

    @Override
    protected void onException(Exception e) {
        seenThrowable = e;
        super.onException(e);
    }

    @Override
    public void afterExec() {
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
    }


    public static <T extends UpdateProcessor> UpdateProcessor wrap(T decoratee, Transactional transactional) {
        return new UpdateProcessorDecoratorTxn<>(decoratee, transactional);
    }

}
