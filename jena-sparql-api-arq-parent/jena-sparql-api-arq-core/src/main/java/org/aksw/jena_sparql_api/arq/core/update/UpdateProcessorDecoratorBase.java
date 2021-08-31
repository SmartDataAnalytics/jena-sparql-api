package org.aksw.jena_sparql_api.arq.core.update;

import org.apache.jena.update.UpdateProcessor;

public class UpdateProcessorDecoratorBase<T extends UpdateProcessor>
    implements UpdateProcessorDecorator
{
    protected T delegate;

    public UpdateProcessorDecoratorBase(T delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public UpdateProcessor getDelegate() {
        return delegate;
    }

    protected void beforeExec() {

    }

    protected void afterExec() {

    }

    protected void onException(Exception e) {
    }


    @Override
    public void execute() {
        beforeExec();
        try {
            delegate.execute();
        } catch(Exception e) {
            onException(e);
            throw e;
        } finally {
            afterExec();
        }
    }




}
