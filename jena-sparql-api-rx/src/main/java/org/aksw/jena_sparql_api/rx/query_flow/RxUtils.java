package org.aksw.jena_sparql_api.rx.query_flow;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;

import org.reactivestreams.Subscription;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.core.FlowableTransformer;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Scheduler.Worker;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.internal.fuseable.SimpleQueue;
import io.reactivex.rxjava3.internal.queue.SpscArrayQueue;

public class RxUtils {
    /**
     * A 'poison' is an object that serves as an end marker on blocking queues
     */
    public static final Object POISON = new Object();

    @SuppressWarnings("unchecked")
    public static <T> T poison() {
        return (T)POISON;
    }

    public static Map<String, AtomicInteger> nameMap = new ConcurrentHashMap<>();

    public static <T> FlowableTransformer<T, T> counter(String name, long interval) {
          return RxUtils.createTransformer(emitter -> new FlowBase<T>(emitter) {
              int id;
              long i[] = {0};
              long startTimeMillis;

              public void onSubscribe(Subscription s) {
                  AtomicInteger n = nameMap.computeIfAbsent(name, k -> new AtomicInteger());
                  id = n.incrementAndGet();
                  startTimeMillis = System.currentTimeMillis();

                  super.onSubscribe(s);
              };

              @Override
              public void onNext(T item) {
                  long elapsed = System.currentTimeMillis() - startTimeMillis;

                  if(i[0] % interval == 0) {
                      System.err.println("On " + name + "-" + id + " seen item count = " + i[0] + " - throughput: " + (i[0] / (elapsed * 0.001f)));
                  }
                  ++i[0];
                  emitter.onNext(item);
              }
          });

      }

//    public static <T> FlowableTransformer<T, T> queuedObserveOn(Scheduler scheduler, int capacity) {
//        return upstream -> upstream
////                .map(x -> x)
//                .compose(queueProducer(capacity))
//                .doOnNext(x -> System.err.println("Passing on queue: " + x.size()))
////                .observeOn(Schedulers.newThread())
//                .observeOn(scheduler)
//                .compose(queueConsumer());
//    }


    public static <T> void put(SimpleQueue<T> queue, T item) throws InterruptedException {
        if(!queue.offer(item)) {
            synchronized (queue) {
                while(!queue.offer(item)) {
                    queue.notifyAll();
                    queue.wait();
                }
            }
        }
    }

    public static <T> T take(SimpleQueue<T> queue) throws Throwable {
        T result = queue.poll();
        if(result == null) {
            synchronized(queue) {
                queue.notifyAll();
                while((result = queue.poll()) == null) {
//                    System.out.println("Waiting for items");
                    queue.wait(100);
                    queue.notifyAll();
                    break;
                }
            }
        }
        return result;
    }

    public static <T> FlowableTransformer<T, T> queuedObserveOn(Scheduler scheduler, int capacity) {

        return upstream -> Flowable.create(new FlowableOnSubscribe<T>() {
                @Override
                public void subscribe(FlowableEmitter<T> downstream) throws Exception {
                    //BlockingQueue<T> queue = new LinkedBlockingQueue<T>(capacity);
//                    BlockingQueue<T> queue = new ArrayBlockingQueue<>(capacity);
                    SimpleQueue<T> queue = new SpscArrayQueue<>(capacity);

                    Disposable[] disposable = {null};

                    Worker worker = scheduler.createWorker();

                    Runnable action = () -> {
                        while(!Thread.interrupted()) {
                            T item;
                            try {
                                // If the worker is backed by a thread pool, this may give that pool
                                // the opportunity to pick another task if no items were delivered in time
                                // This may prevent dead locks however at the cost of greatly degraded performance
                                //item = queue.poll(100, TimeUnit.MILLISECONDS);
                                item = take(queue);

                            } catch (Throwable e1) {
                                throw new RuntimeException(e1);
                            }
        //                    System.out.println("Queue state of "  + System.identityHashCode(queue) + ": " + queue.size() + " items seen: " + (++i[0]));
                            if(item == POISON) {
        //                        System.out.println("Queue completed");
                                downstream.onComplete();
                                worker.dispose();
                                break;
                            }
                            else if(item == null) {
                                break;
                            }
                            else {
                                downstream.onNext(item);

//                                if(queue.remainingCapacity() == 0) {
//                                    System.err.println("WARN: Consumer too slow");
//                                }
                            }
                        }
                    };

                    worker.schedulePeriodically(action, 0, 0, TimeUnit.MILLISECONDS);
//                    worker.schedule(action);

                    disposable[0] = upstream
//                            .delay(0, TimeUnit.MILLISECONDS)
                            .subscribe(
                                x -> {
//                                    Thread.sleep(1);
//                                    System.out.println("Putting on queue "  + System.identityHashCode(queue) + ": " + queue.size());
                                    //queue.put(x);
                                    put(queue, x);
                                },
                                e -> downstream.onError(e),
                                () -> {
                                    //queue.put((T)POISON);
                                    put(queue, (T)POISON);
                                    synchronized (queue) {
                                        queue.notifyAll();
                                    }
                                }
                        );

                        downstream.setDisposable(disposable[0]);


                }
            }, BackpressureStrategy.ERROR);


//      Flowable.<T, BlockingQueue<T>>generate(
//              () -> queue,
//              (q, e) -> {
//                   T item = q.take();
////                  T item = q.poll();
////                  System.out.println("Queue state of "  + System.identityHashCode(queue) + ": " + queue.size() + " items seen: " + (++i[0]));
//                  if(item == POISON) {
////                      System.out.println("Queue completed");
//                      e.onComplete();
//                  }
//                  else if(item == null) {
//                      // nothing to do
//                  }
//                  else {
//                      e.onNext(item);
//                  }
//              },
//              q -> { disposable[0].dispose(); }
//      )
//      .subscribeOn(scheduler)
////      .delay(0, TimeUnit.MILLISECONDS)
//      .subscribe(x -> downstream.onNext(x), t -> downstream.onError(t), () -> downstream.onComplete());
//      ;

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
                    System.err.println("Putting to queue " + System.identityHashCode(queue) + " state: " + queue.size());
                    queue.put(item);
                    System.err.println("Returned (and possibly woke up) from put");
                    return queue;
                })
                .doOnComplete(() -> queue.put((T)POISON))
                ;

    }

    public static <T> Flowable<T> fromBlockingQueue(BlockingQueue<T> queue, Predicate<? super T> isPoison) {
        return Flowable.generate(
                () -> queue,
                (q, e) -> {
                    T item = q.take();
                    if (isPoison.test(item)) {
                        e.onComplete();
                    } else {
                        e.onNext(item);
                    }
                },
                q -> {}
                );
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

//                        BlockingQueue<T> queue = null;

                        public void drain(BlockingQueue<T> queue) throws InterruptedException {
//                            this.queue = queue;
                            T item;
//                          while(!queue.isEmpty() && !child.isCancelled()) {
                          while((item = queue.take()) != null && !child.isCancelled()) {
//                            System.err.println("" + Thread.currentThread() + "- QueueState " + System.identityHashCode(queue) + ": " + queue.size());

//                              try {
////                                  item = queue.poll()();
//                              } catch (InterruptedException e) {
//                                  throw new RuntimeException(e);
//                              }
                              if(item == POISON) {
                                  System.err.println("POISON seen");
//                                  child.onComplete();
                              } else {
                                  System.out.println("Passed on an item " + "- QueueState " + System.identityHashCode(queue) + ": " + queue.size());
                                  child.onNext(item);
                              }
                          }
//                          s.request(1);
                        }

                        @Override
                        public void onNext(BlockingQueue<T> queue) {
                            try {
                                drain(queue);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        @Override
                        public void onError(Throwable t) {
                            child.onError(t);
                        }

                        @Override
                        public void onComplete() {
//                            if(queue != null) {
//                                drain(queue);
//                            }
                            System.err.println("On complete called");
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

    /**
     * Consume a flow by mapping it to empty maybes as long as there is no error.
     * On error emit a maybe that hold the occurred exception.
     * Uses blockingGet on the single result
     *
     *
     *
     * @param flowable
     */
    public static void consume(Flowable<?> flowable) {
        Flowable<Throwable> tmp = flowable
                //.mapOptional(x -> Optional.<Throwable>empty())
                .concatMapMaybe(x -> Maybe.<Throwable>empty())
                .onErrorReturn(t -> t);

        Throwable e = tmp.singleElement().blockingGet();
        if(e != null) {
            throw new RuntimeException(e);
        }
    }
}