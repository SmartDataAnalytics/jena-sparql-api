package org.aksw.jena_sparql_api.rx.query_flow;

import org.reactivestreams.Subscription;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableSubscriber;

/**
 * Utility base class for FlowableSubscribers which wraps a FlowableEmitter.
 *
 * There may be a similar base class in the RxJava framework itself, however so far I did not find it ~ Claus
 *
 * @author raven
 *
 * @param <T>
 */
public abstract class FlowBase<T>
    implements FlowableSubscriber<T>
{
    protected FlowableEmitter<T> emitter;

    public FlowBase(FlowableEmitter<T> emitter) {
        super();
        this.emitter = emitter;
    }

    @Override
    public void onError(Throwable t) {
        emitter.onError(t);
    }

    @Override
    public void onSubscribe(@NonNull Subscription s) {
        emitter.setCancellable(s::cancel);
        s.request(Long.MAX_VALUE);
    }

    @Override
    public void onComplete() {
        emitter.onComplete();
    }
}