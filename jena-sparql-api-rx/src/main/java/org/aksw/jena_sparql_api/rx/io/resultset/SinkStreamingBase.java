package org.aksw.jena_sparql_api.rx.io.resultset;

import org.aksw.commons.util.lifecycle.LifeCycleBase;

public abstract class SinkStreamingBase<T>
    extends LifeCycleBase
    implements SinkStreaming<T>
{
    public final void send(T item) {
        expectStarted();
        sendActual(item);
    }

    protected void startActual() {};
    protected void finishActual() {};

    protected abstract void sendActual(T item);
}