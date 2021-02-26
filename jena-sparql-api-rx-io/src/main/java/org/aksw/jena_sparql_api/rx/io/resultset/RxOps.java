package org.aksw.jena_sparql_api.rx.io.resultset;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.LongStream;

import org.aksw.jena_sparql_api.rx.op.OperatorLocalOrder;
import org.apache.jena.ext.com.google.common.collect.Maps;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableTransformer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class RxOps {
    /**
     * Factory method for yielding a FlowableTransformer that applies a given flatMap function in parallel
     * but apply local ordering so that items are emitted in order
     *
     * @param <I>
     * @param <O>
     * @param flatMapper
     * @return
     */
    public static <I, O> FlowableTransformer<I, O> createParallelMapperOrdered(
            Function<? super I, O> mapper) {
        return in -> in
//            .map(x -> Maps.immutableEntry(x, 0l))
            .zipWith(() -> LongStream.iterate(0, i -> i + 1).iterator(), Maps::immutableEntry)
            .parallel() //Runtime.getRuntime().availableProcessors(), 8) // Prefetch only few items
            .runOn(Schedulers.io())
            //.observeOn(Schedulers.computation())
            .map(e -> {
                I before = e.getKey();
                O after = mapper.apply(before);
                Entry<O, Long> r = new SimpleEntry<>(after, e.getValue());
                return r;
            })
            .sequential()
            //.lift(OperatorLocalOrder.create(0l, i -> i + 1, (a, b) -> a - b, Entry::getValue))
            .lift(OperatorLocalOrder.forLong(0l, Entry::getValue))
            .map(Entry::getKey);
    }

    public static <I, O> FlowableTransformer<I, O> createParallelFlatMapperOrdered(
            Function<? super I, ? extends Iterable<? extends O>> mapper) {
    	return in -> in.compose(createParallelMapperOrdered(mapper))
    			.concatMap(Flowable::fromIterable);
    }

}
