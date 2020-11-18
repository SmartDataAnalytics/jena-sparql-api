package org.aksw.jena_sparql_api.rx;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Emitter;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.core.FlowableTransformer;

/**
 * USE OperatorLocalOrder
 *
 * A subscriber that performs local ordering of the items by their sequence id.
 * Local ordering means, that ordering is accomplished in a streaming fashion
 * without the need of a global view of *all* items.
 * This is the case when items arrive "mostly" sequentially, with some "outliers" arriving out of order,
 * as it can happen e.g. due to network delay.
 *
 * This implementation uses a 'extractSeqId' lambda to obtain an item's sequence id,
 * and 'incrementSeqId' in order to find out next id to expect.
 * This class then caches all arriving items in memory until
 * an item with the expected id arrives. In this case that item and all consecutive
 * ones are emitted and removed from the cache.
 *
 * Example Usage:
 * <pre>{@code
 * flowable
 *   .zipWith(() -> LongStream.iterate(0, i -> i + 1).iterator(), Maps::immutableEntry)
 *   .map(...)
 *   .compose(FlowableTransformerLocalOrdering.transformer(0l, i -> i + 1, Entry::getValue))
 * }</pre>
 *
 * @author raven May 12, 2018
 *
 * @param <T>
 * @param <S>
 */
public class FlowableTransformerLocalOrderingOld<T, S>
    //implements Subscriber<T>
    implements Emitter<T>
{
    private static final Logger logger = LoggerFactory.getLogger(FlowableTransformerLocalOrderingOld.class);

    protected FlowableEmitter<? super T> delegate; //Consumer<? super T> delegate;

    protected Function<? super T, ? extends S> extractSeqId;
    protected Function<? super S, ? extends S> incrementSeqId;

    protected BiFunction<? super S, ? super S, ? extends Number> distanceFn;

//    protected int maxIdDistance = 16;

    //protected DiscreteDomain<S> discreteDomain;

    protected S expectedSeqId;
    protected boolean isComplete = false;

    protected NavigableMap<S, T> seqIdToValue;


    public FlowableTransformerLocalOrderingOld(
            S expectedSeqId,
            Function<? super S, ? extends S> incrementSeqId,
            BiFunction<? super S, ? super S, ? extends Number> distanceFn,
            Function<? super T, ? extends S> extractSeqId,
            FlowableEmitter<? super T> delegate) {
        super();
        this.extractSeqId = extractSeqId;
        this.incrementSeqId = incrementSeqId;
        this.distanceFn = distanceFn;
        this.expectedSeqId = expectedSeqId;
        this.delegate = delegate;

        this.seqIdToValue = new TreeMap<>((a, b) -> distanceFn.apply(a, b).intValue());
    }

    public void onError(Throwable throwable) {
        delegate.onError(throwable);

        //throw new RuntimeException(throwable);
    }


    public void onComplete() {
        isComplete = true;

        // If there are no more entries in the map, complete the delegate immediately
        if(seqIdToValue.isEmpty()) {
            delegate.onComplete();
        }

        // otherwise, the onNext method has to handle completion
    }

    public void onNext(T value) {
//        if(delegate.isCancelled()) {
//            throw new RuntimeException("Downstream cancelled");
//        }

        S seqId = extractSeqId.apply(value);

//        System.err.println("ENCOUNTERED CONTRIB " + seqId + " WITH QUEUE size " + seqIdToValue.keySet().size());
        // If complete, the seqId must not be higher than the latest seen one
        if(isComplete) {
            if(seqIdToValue.isEmpty()) {
                onError(new RuntimeException("Sanity check failed: Call to onNext encountered after completion."));
            }


            S highestSeqId = seqIdToValue.descendingKeySet().first();

            if(distanceFn.apply(seqId, highestSeqId).intValue() > 0) {
                onError(new RuntimeException("Sequence was marked as complete with id " + highestSeqId + " but a higher id was encountered " + seqId));
            }
        }

        boolean checkForExistingKeys = true;
        if(checkForExistingKeys) {
            if(seqIdToValue.containsKey(seqId)) {
                onError(new RuntimeException("Already seen an item with id " + seqId));
            }
        }

        // If the distance is too great block the thread
        //synchronized (this) {
//            int dd;
//            while((dd = distanceFn.apply(seqId, expectedSeqId).intValue()) > maxIdDistance) {
//                System.err.println("DISTANCE FROM expected " + expectedSeqId + " TO contrib " + seqId + " IS " + dd + " GOING TO SLEEP " + Thread.currentThread());
//
//                try {
//                    //System.err.println("DISTANCE " + d + " TO GREAT - SLEEPING " + Thread.currentThread());
//                    // this.wait();
//                    Thread.sleep(100);
//                    break;
//                } catch(InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//
////                if(delegate.isCancelled()) {
////                    throw new RuntimeException("Downstream cancelled");
////                }
//            }
//
//            System.err.println("DISTANCE FROM expected " + expectedSeqId + " TO contrib " + seqId + " IS " + dd + " " + Thread.currentThread());
//        // }

        //synchronized(this) {
            // Add item to the map
            seqIdToValue.put(seqId, value);

            // Consume consecutive items from the map
            Iterator<Entry<S, T>> it = seqIdToValue.entrySet().iterator();
            while(it.hasNext()) {
//                if(delegate.isCancelled()) {
//                    throw new RuntimeException("Downstream cancelled");
//                }


                Entry<S, T> e = it.next();
                S s = e.getKey();
                T v = e.getValue();

                int d = distanceFn.apply(s, expectedSeqId).intValue();
                if(d == 0) {
                    it.remove();
                    delegate.onNext(v);
                    expectedSeqId = incrementSeqId.apply(expectedSeqId);
                    // this.notifyAll();
                    //System.out.println("expecting seq id " + expectedSeqId);
                } else if(d < 0) {
                    // Skip values with a lower id
                    // TODO Add a flag to emit onError event
                    logger.warn("Should not happen: received id " + s + " which is lower than the expected id " + expectedSeqId);
                    it.remove();
                } else { // if d > 0
                    // Wait for the next sequence id
                    logger.trace("Received id " + s + " while waiting for expected id " + expectedSeqId);
                    break;
                }
            }

            // If the completion mark was set and all items have been emitted, we are done
            if(isComplete && seqIdToValue.isEmpty()) {
                delegate.onComplete();
            }
        // }
    }


    public static <T> Emitter<T> forLong(long initiallyExpectedId, Function<? super T, ? extends Long> extractSeqId, FlowableEmitter<? super T> delegate) {
        return new FlowableTransformerLocalOrderingOld<T, Long>(initiallyExpectedId, id -> Long.valueOf(id.longValue() + 1l), (a, b) -> a - b, extractSeqId, delegate);
    }

    public static <T, S extends Comparable<S>> FlowableTransformerLocalOrderingOld<T, S> wrap(S initiallyExpectedId, Function<? super S, ? extends S> incrementSeqId, BiFunction<? super S, ? super S, ? extends Number> distanceFn, Function<? super T, ? extends S> extractSeqId, FlowableEmitter<? super T> delegate) {
        return new FlowableTransformerLocalOrderingOld<T, S>(initiallyExpectedId, incrementSeqId, distanceFn, extractSeqId, delegate);
    }

    public static <T, S extends Comparable<S>> FlowableTransformer<T, T> transformer(S initiallyExpectedId, Function<? super S, ? extends S> incrementSeqId, BiFunction<? super S, ? super S, ? extends Number> distanceFn, Function<? super T, ? extends S> extractSeqId) {

        return upstream -> {
            Flowable<T> result = Flowable.create(new FlowableOnSubscribe<T>() {

                @Override
                public void subscribe(FlowableEmitter<T> e) throws Exception {
                    FlowableTransformerLocalOrderingOld<T, S> tmp = wrap(
                            initiallyExpectedId,
                            incrementSeqId,
                            distanceFn,
                            extractSeqId,
                            e);

                    upstream.subscribe(new FlowableSubscriber<T>() {
                        @Override
                        public void onSubscribe(Subscription s) {
                            e.setCancellable(s::cancel);
                            s.request(Long.MAX_VALUE);
                        }

                        @Override
                        public void onNext(T t) {
                            tmp.onNext(t);
                        }

                        @Override
                        public void onError(Throwable t) {
                            tmp.onError(t);
                        }

                        @Override
                        public void onComplete() {
                            tmp.onComplete();
                        }

                    });

//                    Disposable[] d = {null};
//                    d[0] = upstream.subscribe(
//                            item -> {
//                                if(e.isCancelled()) {
//                                    Disposable x = d[0];
//                                    if(x != null) {
//                                        x.dispose();
//                                    }
//                                } else {
//                                    tmp.onNext(item);
//                                }
//                            },
//                            Exceptions::propagate,
//                            // tmp::onError,
//                            tmp::onComplete);
////                    e.setCancellable(() -> {
////                        System.out.println("CANCELLED");
////                    });
//                    e.setDisposable(d[0]);
//                    // System.out.println("Done");
//                    // FIXME Something might be broken in the design, as
//                    // upstream.subscribe(tmp) does NOT work


                }
            }, BackpressureStrategy.BUFFER);

            return result;
        };
    }

    /*
    public static <T, S extends Comparable<S>> ParallelTransformer<T, T> parallelTransformer(S initiallyExpectedId, Function<? super S, ? extends S> incrementSeqId, BiFunction<? super S, ? super S, ? extends Number> distanceFn, Function<? super T, ? extends S> extractSeqId) {
        return upstream -> {
            Flowable<T> result = Flowable.create(new FlowableOnSubscribe<T>() {
                @Override
                public void subscribe(FlowableEmitter<T> e) throws Exception {
                    FlowableTransformerLocalOrdering<T, S> tmp = wrap(
                            initiallyExpectedId,
                            incrementSeqId,
                            distanceFn,
                            extractSeqId,
                            e);

                    upstream.subscribe(
                            tmp::onNext,
                            tmp::onError,
                            tmp::onComplete);

                    // FIXME Something might be broken in the design, as
                    // upstream.subscribe(tmp) does NOT work
                }
            }, BackpressureStrategy.BUFFER);

            return result;
        };
    }*/
}

