package org.aksw.jena_sparql_api.rx;

import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.jena.ext.com.google.common.collect.Maps;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;
import io.reactivex.rxjava3.core.FlowableOperator;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.core.FlowableTransformer;

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
 *			.lift(new OperatorOrderedGroupBy<Entry<Integer, Integer>, Integer, List<Integer>>(Entry::getKey, ArrayList::new, (acc, e) -> acc.add(e.getValue())));
 *
 * }</pre>
 *
 * @author raven
 *
 * @param <T> Item type
 * @param <K> Group key type
 * @param <V> accumulator type
 */
public final class OperatorOrderedGroupBy<T, K, V>
    implements FlowableOperator<Entry<K, V>, T> {

    protected Function<? super T, ? extends K> getGroupKey;
    protected BiFunction<? super K, ? super K, Boolean> groupKeyCompare;
    protected Function<? super K, ? extends V> accCtor;
    protected BiConsumer<? super V, ? super T> accAdd;

    public OperatorOrderedGroupBy(
            Function<? super T, ? extends K> getGroupKey,
            Function<? super K, ? extends V> accCtor,
            BiConsumer<? super V, ? super T> accAdd) {
        this(getGroupKey, Objects::equals, accCtor, accAdd);
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
    public Subscriber<? super T> apply(Subscriber<? super Entry<K, V>> child) throws Exception {
        return new Op<>(child, getGroupKey, groupKeyCompare, accCtor, accAdd);
    }

    static final class Op<T, K, V> implements FlowableSubscriber<T>, Subscription {
        final Subscriber<? super Entry<K, V>> child;

        protected Subscription s;

        protected Function<? super T, ? extends K> getGroupKey;
        protected BiFunction<? super K, ? super K, Boolean> groupKeyCompare;
        protected Function<? super K, ? extends V> accCtor;
        protected BiConsumer<? super V, ? super T> accAdd;

        protected K priorKey;
        protected K currentKey;

        protected V currentAcc = null;


        public Op(Subscriber<? super Entry<K, V>> child,
                Function<? super T, ? extends K> getGroupKey,
                BiFunction<? super K, ? super K, Boolean> groupKeyCompare,
                Function<? super K, ? extends V> accCtor,
                BiConsumer<? super V, ? super T> accAdd) {
            super();
            this.child = child;
            this.getGroupKey = getGroupKey;
            this.groupKeyCompare = groupKeyCompare;
            this.accCtor = accCtor;
            this.accAdd = accAdd;
        }

        @Override
        public void onSubscribe(Subscription s) {
            this.s = s;
            child.onSubscribe(this);
        }

        @Override
        public void onNext(T item) {
            currentKey = getGroupKey.apply(item);

            if(currentAcc == null) {
                // First time init
                priorKey = currentKey;
                currentAcc = accCtor.apply(currentKey);

                Objects.requireNonNull(currentAcc, "Got null for an accumulator");
            } else if (!groupKeyCompare.apply(priorKey, currentKey)) { //(!Objects.equals(priorKey, currentKey)) {

                child.onNext(Maps.immutableEntry(priorKey, currentAcc));

                currentAcc = accCtor.apply(currentKey);
            }
            accAdd.accept(currentAcc, item);
            priorKey = currentKey;
        }

        @Override
        public void onError(Throwable e) {
            child.onError(e);
        }

        @Override
        public void onComplete() {
            if(currentAcc != null) {
                child.onNext(Maps.immutableEntry(currentKey, currentAcc));
            }

            child.onComplete();
        }

        @Override
        public void cancel() {
            s.cancel();
        }

        @Override
        public void request(long n) {
            s.request(Long.MAX_VALUE);
        }
    }



    public FlowableTransformer<T, Entry<K, V>> transformer() {

        return upstream -> {
            Flowable<Entry<K, V>> result = Flowable.create(new FlowableOnSubscribe<Entry<K, V>>() {

                @Override
                public void subscribe(FlowableEmitter<Entry<K, V>> child) throws Exception {
                    upstream.subscribe(new FlowableSubscriber<T>() {

                        protected K priorKey;
                        protected K currentKey;

                        protected V currentAcc = null;

//                        protected Subscription s;
                        @Override
                        public void onSubscribe(Subscription s) {
//                            this.s = s;
                            child.setCancellable(s::cancel);
                            s.request(Long.MAX_VALUE);
//                            s.request(1);
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

                                child.onNext(Maps.immutableEntry(priorKey, currentAcc));

                                currentAcc = accCtor.apply(currentKey);
                            }
                            accAdd.accept(currentAcc, item);
                            priorKey = currentKey;

//                            s.request(1);
                        }

                        @Override
                        public void onError(Throwable t) {
                            child.onError(t);
                        }

                        @Override
                        public void onComplete() {
                            if(currentAcc != null) {
                                child.onNext(Maps.immutableEntry(currentKey, currentAcc));
                            }

                            child.onComplete();
                        }

                    });

                }
            }, BackpressureStrategy.BUFFER);

            return result;
        };
    }
}
