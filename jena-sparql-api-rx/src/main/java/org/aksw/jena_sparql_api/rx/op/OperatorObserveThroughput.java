package org.aksw.jena_sparql_api.rx.op;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.aksw.jena_sparql_api.rx.query_flow.RxUtils;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.rxjava3.core.FlowableOperator;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.internal.subscriptions.SubscriptionHelper;

/**
 * Operator that fires a callback every time it has seen a configurable number of items.
 *
 * @author raven
 *
 * @param <T>
 */
public class OperatorObserveThroughput<T>
    implements FlowableOperator<T, T>
{
    protected String name;
    protected long interval;

    public static class ThroughputEvent {
        public ThroughputEvent(long eventInterval, String name, long instanceId, double elapsedSeconds,
                long totalSeenItemCount) {
            super();
            this.eventInterval = eventInterval;
            this.name = name;
            this.instanceId = instanceId;
            this.elapsedSeconds = elapsedSeconds;
            this.totalSeenItemCount = totalSeenItemCount;
        }

        public long eventInterval;
        public String name;
        long instanceId;
        double elapsedSeconds;
        long totalSeenItemCount;
    }

    protected Consumer<ThroughputEvent> eventHandler;

    public static void defaultThroughputEventHandler(ThroughputEvent event) {
        System.err.println("On " + event.name + "-" + event.instanceId + " seen item count = " + event.totalSeenItemCount + " - throughput: " + (event.totalSeenItemCount / (event.elapsedSeconds)) + " items per second");
    }

    public static <T> OperatorObserveThroughput<T> create(String name, long interval) {
        return new OperatorObserveThroughput<>(name, interval, OperatorObserveThroughput::defaultThroughputEventHandler);
    }

    public static <T> OperatorObserveThroughput<T> create(String name, long interval, Consumer<ThroughputEvent> eventHandler) {
        return new OperatorObserveThroughput<>(name, interval, eventHandler);
    }

    public OperatorObserveThroughput(String name, long interval, Consumer<ThroughputEvent> eventHandler) {
        super();
        this.name = name;
        this.interval = interval;
        this.eventHandler = eventHandler;
    }

    @Override
    public Subscriber<? super T> apply(Subscriber<? super T> downstream) throws Exception {
        return new SubscriberImpl(downstream);
    }


    public class SubscriberImpl
        implements FlowableSubscriber<T>, Subscription
    {
        protected Subscriber<? super T> downstream;
        protected Subscription upstream;
        protected AtomicLong pending = new AtomicLong();

        int id;
        protected AtomicLong seenItems = new AtomicLong();
        long startTimeMillis;


        public SubscriberImpl(Subscriber<? super T> downstream) {
            this.downstream = downstream;
        }

        @Override
        public void onSubscribe(Subscription s) {
            AtomicInteger n = RxUtils.nameMap.computeIfAbsent(name, k -> new AtomicInteger());
            id = n.incrementAndGet();
            startTimeMillis = System.currentTimeMillis();

            if (upstream != null) {
                s.cancel();
            } else {
                upstream = s;
                downstream.onSubscribe(this);
            }
        }

        @Override
        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                pending.addAndGet(n);
                upstream.request(1);
            }
        }

        @Override
        public void onNext(T item) {
            long elapsed = System.currentTimeMillis() - startTimeMillis;

            long counter = seenItems.getAndIncrement();
            if(counter % interval == 0) {
                double totalElapsedSeconds = elapsed * 0.001;
                ThroughputEvent event = new ThroughputEvent(interval, name, id, totalElapsedSeconds, counter);
                eventHandler.accept(event);
            }

            downstream.onNext(item);
            long remaining = pending.decrementAndGet();
            if(remaining > 0) {
                upstream.request(1);
            }
        }

        @Override
        public void onComplete() {
            downstream.onComplete();
        }

        @Override
        public void onError(Throwable t) {
            downstream.onError(t);
        }

        @Override
        public void cancel() {
            upstream.cancel();
        }

    }
}
