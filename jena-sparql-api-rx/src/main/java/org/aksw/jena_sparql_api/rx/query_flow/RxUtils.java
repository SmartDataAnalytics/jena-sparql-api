package org.aksw.jena_sparql_api.rx.query_flow;

import java.util.function.Function;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.core.FlowableTransformer;

public class RxUtils {

    /**
     * Utils method to create a transformer from a function that takes a FlowableEmitter and
     * yields a FlowableSubscriber from it.
     *
     * @param <I>
     * @param <O>
     * @param fsSupp
     * @return
     */
    public static <I, O> FlowableTransformer<I, O> createTransformer(Function<? super FlowableEmitter<O>, ? extends FlowableSubscriber<I>> fsSupp) {
        return upstream -> {
            Flowable<O> result = Flowable.create(new FlowableOnSubscribe<O>() {
                @Override
                public void subscribe(FlowableEmitter<O> emitter) throws Exception {
                    FlowableSubscriber<I> subscriber = fsSupp.apply(emitter);
                    upstream.subscribe(subscriber);
                }
            }, BackpressureStrategy.BUFFER);

            return result;
        };
    }
}