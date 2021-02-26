package org.aksw.jena_sparql_api.rx.op;

import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.apache.jena.ext.com.google.common.collect.Maps;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableOperator;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.core.FlowableTransformer;
import io.reactivex.rxjava3.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava3.internal.util.BackpressureHelper;

/**
 * Sequential group by; somewhat similar to .toListWhile() but with dedicated support for
 * group keys and accumulators
 *
 *
 * The items' group keys are expected to arrive in order, hence only a single accumulator is active at a time.
 *
 * <pre>{@code
 * 		Flowable<Entry<Integer, List<Integer>>> list = Flowable
 *			.range(0, 10)
 *			.map(i -> Maps.immutableEntry((int)(i / 3), i))
 *			.lift(OperatorOrderedGroupBy.<Entry<Integer, Integer>, Integer, List<Integer>>create(Entry::getKey, ArrayList::new, (acc, e) -> acc.add(e.getValue())));
 *
 * }</pre>
 *
 * @author raven
 *
 * @param <T> Item type
 * @param <K> Group key type
 * @param <V> Accumulator type
 */
public final class FlowableOperatorSequentialGroupBy<T, K, V>
    implements FlowableOperator<Entry<K, V>, T> {

	/* Function to derive a group key from an item in the flow */
    protected Function<? super T, ? extends K> getGroupKey;
    
    /* Comparision whether two group keys are equal */
    protected BiPredicate<? super K, ? super K> groupKeyCompare;
    
    /* Constructor function for accumulators. Function argument is the group key */
    protected Function<? super K, ? extends V> accCtor;
    
    /* Add an item to the accumulator */
    protected BiConsumer<? super V, ? super T> accAdd;

    public static <T, K, V> FlowableOperatorSequentialGroupBy<T, K, V> create(
            Function<? super T, ? extends K> getGroupKey,
            Function<? super K, ? extends V> accCtor,
            BiConsumer<? super V, ? super T> accAdd) {
        return create(getGroupKey, Objects::equals, accCtor, accAdd);
    }

    public static <T, K, V> FlowableOperatorSequentialGroupBy<T, K, V> create(
            Function<? super T, ? extends K> getGroupKey,
            BiPredicate<? super K, ? super K> groupKeyCompare,
            Function<? super K, ? extends V> accCtor,
            BiConsumer<? super V, ? super T> accAdd) {
        return new FlowableOperatorSequentialGroupBy<>(getGroupKey, groupKeyCompare, accCtor, accAdd);
    }

    public FlowableOperatorSequentialGroupBy(
            Function<? super T, ? extends K> getGroupKey,
            BiPredicate<? super K, ? super K> groupKeyCompare,
            Function<? super K, ? extends V> accCtor,
            BiConsumer<? super V, ? super T> accAdd) {
        super();
        this.getGroupKey = getGroupKey;
        this.groupKeyCompare = groupKeyCompare;
        this.accCtor = accCtor;
        this.accAdd = accAdd;
    }

    @Override
    public Subscriber<? super T> apply(Subscriber<? super Entry<K, V>> downstream) throws Exception {
        return new SubscriberImpl(downstream);
    }

    /**
     * Deprecated; Prefer using {@link Flowable#lift(FlowableOperator)} over {@link Flowable#compose(FlowableTransformer)}
     */
    @Deprecated
    public FlowableTransformer<T, Entry<K, V>> transformer() {
        return upstream -> upstream.lift(this);
    }

    public class SubscriberImpl
        implements FlowableSubscriber<T>, Subscription
    {
        protected Subscriber<? super Entry<K, V>> downstream;
        protected Subscription upstream;

        protected K priorKey;
        protected K currentKey;

        protected V currentAcc = null;

        protected AtomicLong pending = new AtomicLong();

        public SubscriberImpl(Subscriber<? super Entry<K, V>> downstream) {
           this.downstream = downstream;
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (upstream != null) {
                s.cancel();
            } else {
                upstream = s;
                downstream.onSubscribe(this);
            }
        }

        @Override
        public void onNext(T item) {
//            System.out.println("ONNEXT PENDING: " + pending.get() + " " + Thread.currentThread());
//            if (pending.get() <= 0) {
////                System.out.println("PENDING IS ZERO " + Thread.currentThread());
////                System.out.println("PENDING IS " + pending.get());
//                throw new RuntimeException("Received item without any pending requests");
//            }
            currentKey = getGroupKey.apply(item);

            boolean needMore = true;
            if(currentAcc == null) {
                // First time init
                priorKey = currentKey;
                currentAcc = accCtor.apply(currentKey);

                Objects.requireNonNull(currentAcc, "Got null for an accumulator");
            } else if(!groupKeyCompare.test(priorKey, currentKey)) {

                Entry<K, V> e = Maps.immutableEntry(priorKey, currentAcc);
//                System.out.println("Passing on " + e);
                needMore = pending.decrementAndGet() > 0;
                downstream.onNext(e);

                currentAcc = accCtor.apply(currentKey);
            }
            accAdd.accept(currentAcc, item);
            priorKey = currentKey;

            if (needMore) {
                upstream.request(1);
            }

        }

        @Override
        public void onError(Throwable t) {
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            if(currentAcc != null) {
                // System.out.println("EMITTED ITEM ON COMPLETE");
                downstream.onNext(Maps.immutableEntry(currentKey, currentAcc));
            }

            downstream.onComplete();
        }

        @Override
        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                BackpressureHelper.add(pending, n);
//                pending.addAndGet(n);
//                System.out.println("BEFORE REQUESTED " + n + " total pending " + pending.get() + " " + Thread.currentThread());
                upstream.request(1);
//                System.out.println("AFTER REQUESTED " + n + " total pending " + pending.get() + " " + Thread.currentThread());
            }
        }

        @Override
        public void cancel() {
            upstream.cancel();
        }
    }
}
