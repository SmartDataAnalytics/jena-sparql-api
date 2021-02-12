package org.aksw.jena_sparql_api.rx.io.resultset;

public abstract class SinkStreamingForwarding<T>
    implements SinkStreaming<T>
{
    protected abstract SinkStreaming<T> getDelegate();

    @Override
    public void send(T item) {
        getDelegate().send(item);
    }

    @Override
    public void flush() {
        getDelegate().flush();
    }

    @Override
    public void close() {
        getDelegate().close();
    }

    @Override
    public void start() {
        getDelegate().start();
    }

    @Override
    public void finish() {
        getDelegate().finish();
    }

}
