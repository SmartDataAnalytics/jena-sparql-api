package org.aksw.jena_sparql_api.rx.io.resultset;

public class SinkStreamingAdapter<T>
    extends SinkStreamingBase<T> {

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }

    @Override
    protected void sendActual(T item) {
    }
}
