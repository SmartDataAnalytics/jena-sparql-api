package org.aksw.jena_sparql_api.rx.query_flow;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Function;

import org.reactivestreams.Subscription;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.core.FlowableTransformer;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class RxUtils {
    /**
     * A 'poison' is an object that serves as an end marker on blocking queues
     */
    public static final Object POISON = new Object();

    public static <T> FlowableTransformer<T, T> queuedObserveOn(Scheduler scheduler, int capacity) {
        return upstream -> upstream
//                .map(x -> x)
                .compose(queueProducer(capacity))
//                .observeOn(Schedulers.newThread())
                .observeOn(scheduler)
                .compose(queueConsumer());
    }

    /**
     * Map each item to the same blocking queue instance
     * thereby appending that item to the queue.
     *
     * @param <T>
     * @param capacity
     * @return
     */
    public static <T> FlowableTransformer<T, BlockingQueue<T>> queueProducer(int capacity) {
        BlockingQueue<T> queue = new ArrayBlockingQueue<T>(capacity);

        return upstream -> upstream
                .map(item -> {
//                    if(queue.remainingCapacity() == 0) {
//                        System.err.println("Capacity exhausted");
//                    }
                    queue.put(item);
                    return queue;
                })
                .doOnComplete(() -> queue.put((T)POISON));

    }

    /**
     * Take items from the blocking queue and pass them on to the subscriber
     *
     * @param <T>
     * @return
     */
    public static <T> FlowableTransformer<BlockingQueue<T>, T> queueConsumer() {
        return upstream -> {
            return Flowable.create(new FlowableOnSubscribe<T>() {

                @Override
                public void subscribe(FlowableEmitter<T> child) throws Exception {
                    upstream.subscribe(new FlowableSubscriber<BlockingQueue<T>>() {

//                        protected Subscription s;
                        @Override
                        public void onSubscribe(Subscription s) {
//                            this.s = s;
                            child.setCancellable(s::cancel);
                            s.request(Long.MAX_VALUE);
//                            s.request(1);
                        }

                        @Override
                        public void onNext(BlockingQueue<T> queue) {
                            T item;
                            while(!queue.isEmpty() && !child.isCancelled()) {
//                            while((item = queue.poll()) != null && !child.isCancelled()) {
//                              System.err.println("" + Thread.currentThread() + "- QueueState " + System.identityHashCode(queue) + ": " + queue.size());

                                try {
                                    item = queue.take();
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                if(item == POISON) {
                                    child.onComplete();
                                } else {
    //                                System.out.println("Passed on " + item);
                                    child.onNext(item);
                                }
                            }
//                            s.request(1);
                        }

                        @Override
                        public void onError(Throwable t) {
                            child.onError(t);
                        }

                        @Override
                        public void onComplete() {
                            child.onComplete();
                        }

                    });

                }
            }, BackpressureStrategy.ERROR);
        };
    }

    /**
     * Utils method to create a transformer from a function that takes a FlowableEmitter and
     * yields a FlowableSubscriber from it. Used to slightly reduce boilerplate.
     *
     * @param <I>
     * @param <O>
     * @param fsSupp
     * @return
     */
    public static <I, O> FlowableTransformer<I, O> createTransformer(Function<? super FlowableEmitter<O>, ? extends FlowableSubscriber<I>> fsSupp) {
        return createTransformer(fsSupp, BackpressureStrategy.ERROR);
    }

    public static <I, O> FlowableTransformer<I, O> createTransformer(
            Function<? super FlowableEmitter<O>, ? extends FlowableSubscriber<I>> fsSupp,
            BackpressureStrategy backpressureStrategy) {
        return upstream -> {
            Flowable<O> result = Flowable.create(new FlowableOnSubscribe<O>() {
                @Override
                public void subscribe(FlowableEmitter<O> emitter) throws Exception {
                    FlowableSubscriber<I> subscriber = fsSupp.apply(emitter);
                    upstream.subscribe(subscriber);
                }
            }, backpressureStrategy);

            return result;
        };
    }
}