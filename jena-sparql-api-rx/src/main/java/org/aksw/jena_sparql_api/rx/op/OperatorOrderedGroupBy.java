package org.aksw.jena_sparql_api.rx.op;

import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.jena.ext.com.google.common.collect.Maps;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.rxjava3.core.FlowableOperator;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.core.FlowableTransformer;
import io.reactivex.rxjava3.internal.subscriptions.SubscriptionHelper;

/**
 * Ordered group by; somewhat similar to .toListWhile() but with dedicated support for
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
public final class OperatorOrderedGroupBy<T, K, V>
    implements FlowableOperator<Entry<K, V>, T> {

    protected Function<? super T, ? extends K> getGroupKey;
    protected BiFunction<? super K, ? super K, Boolean> groupKeyCompare;
    protected Function<? super K, ? extends V> accCtor;
    protected BiConsumer<? super V, ? super T> accAdd;

    public static <T, K, V> OperatorOrderedGroupBy<T, K, V> create(
            Function<? super T, ? extends K> getGroupKey,
            Function<? super K, ? extends V> accCtor,
            BiConsumer<? super V, ? super T> accAdd) {
        return create(getGroupKey, Objects::equals, accCtor, accAdd);
    }

    public static <T, K, V> OperatorOrderedGroupBy<T, K, V> create(
            Function<? super T, ? extends K> getGroupKey,
            BiFunction<? super K, ? super K, Boolean> groupKeyCompare,
            Function<? super K, ? extends V> accCtor,
            BiConsumer<? super V, ? super T> accAdd) {
        return new OperatorOrderedGroupBy<>(getGroupKey, groupKeyCompare, accCtor, accAdd);
    }

    public OperatorOrderedGroupBy(
            Function<? super T, ? extends K> getGroupKey,
            BiFunction<? super K, ? super K, Boolean> groupKeyCompare,
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
     * Deprecated; just use .lift() instead of .compose()
     * @return
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
            currentKey = getGroupKey.apply(item);

            if(currentAcc == null) {
                // First time init
                priorKey = currentKey;
                currentAcc = accCtor.apply(currentKey);

                Objects.requireNonNull(currentAcc, "Got null for an accumulator");
            } else if(!groupKeyCompare.apply(priorKey, currentKey)) {//if(!Objects.equals(priorKey, currentKey)) {

                Entry<K, V> e = Maps.immutableEntry(priorKey, currentAcc);
//                System.out.println("Passing on " + e);
                downstream.onNext(e);
                pending.decrementAndGet();

                currentAcc = accCtor.apply(currentKey);
            }
            accAdd.accept(currentAcc, item);
            priorKey = currentKey;

            if(pending.get() > 0) {
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
                downstream.onNext(Maps.immutableEntry(currentKey, currentAcc));
            }

            downstream.onComplete();
        }

        @Override
        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                pending.addAndGet(n);
                upstream.request(1);
            }
        }

        @Override
        public void cancel() {
            upstream.cancel();
        }
    }
}
