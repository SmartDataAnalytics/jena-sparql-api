package org.aksw.combinatorics.algos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.combinatorics.collections.CombinationStack;
import org.aksw.commons.collections.multimaps.BiHashMultimap;

import com.codepoetics.protonpack.functions.TriFunction;
import com.google.common.collect.Multimap;

public class KPermutationsOfNUtils {
//    public static <A, B, S> Stream<CombinationStack<A, B, S>> kPermutationsOfN(Multimap<A, B> mapping) {
//    }

//    public static <K, V> linearMapping() {
//        Linear
//    }
//

    public static <K, V> BiHashMultimap<K, V> create(Multimap<K, V> multimap) {

        BiHashMultimap<K, V> result = new BiHashMultimap<>();
        // TODO Create a putAll method on the bi-multimap
        for(Entry<K, V> entry : multimap.entries()) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public static <A, B> Iterable<Map<A, B>> createIterable(Multimap<A, B> childMapping, Supplier<Map<A, B>> mapSupplier) {
        Iterable<Map<A, B>> result = () -> kPermutationsOfN(childMapping, mapSupplier).iterator();
        //Optional<Iterable<Map<A, B>>> result = Optional.of(tmp);
        return result;
        //IterableUnknownSize<Map<A, B>> result = new IterableUnknownSizeSimple<>(true, tmp);
        //return result;
    }



    //public static <A, B> Stream<CombinationStack<A, B, Object>> kPermutationsOfN(Multimap<A, B> mapping) {
    public static <A, B> Stream<Map<A, B>> kPermutationsOfN(Multimap<A, B> mapping, Supplier<Map<A, B>> mapSupplier) {
        BiHashMultimap<A, B> map = new BiHashMultimap<>();

        // TODO Create a putAll method on the bi-multimap
        for(Entry<A, B> entry : mapping.entries()) {
            map.put(entry.getKey(), entry.getValue());
        }

        List<A> as = new ArrayList<A>(mapping.keySet());

        TriFunction<Object, A, B, Stream<Object>> solutionCombiner = (s, a, b) -> Collections.<Object>singleton(null).stream();

        KPermutationsOfNCandidateLists<A, B, Object> engine =
            new KPermutationsOfNCandidateLists<>(as, map, solutionCombiner);

        Stream<CombinationStack<A, B, Object>> result = engine.stream(null);

        Stream<Map<A, B>> res = result.map(stack -> {
            Map<A, B> r = stack.stream().collect(Collectors.toMap(
                    Entry::getKey, Entry::getValue, (u, v) -> { throw new IllegalStateException(); }, mapSupplier));
            return r;
        });

        return res;
    }


}
