package org.aksw.jena_sparql_api.rx.io.resultset;

import org.apache.jena.atlas.lib.Sink;

public abstract class SinkStreamingWrapper<T>
    extends SinkStreamingBase<T> {

    protected abstract Sink<T> getDelegate();

    @Override
    public void flush() {
        getDelegate().flush();
    }

    @Override
    public void close() {
        getDelegate().close();
    }

    @Override
    protected void sendActual(T item) {
        getDelegate().send(item);
    }

    public static <T> SinkStreaming<T> wrap(Sink<T> delegate) {
        return new SinkStreamingWrapper<T>() {
            @Override
            protected Sink<T> getDelegate() {
                return delegate;
            }
        };
    }
}