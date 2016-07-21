package org.aksw.isomorphism;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Stream;

import org.aksw.commons.collections.multimaps.BiHashMultimap;

import com.codepoetics.protonpack.functions.TriFunction;
import com.google.common.collect.Multimap;

public class KPermutationsOfNUtils {
//    public static <A, B, S> Stream<CombinationStack<A, B, S>> kPermutationsOfN(Multimap<A, B> mapping) {
//    }
    
    public static <A, B, S> Stream<ClusterStack<A, B, S>> kPermutationsOfN(
            Multimap<A, B> mapping,
            Function<B, S> bToClusterKey,
            Function<S, ? extends Collection<B>> clusterKeyToBs
            ) {
        BiHashMultimap<A, B> map = new BiHashMultimap<>();
        
        // TODO Create a putAll method on the bi-multimap
        for(Entry<A, B> entry : mapping.entries()) {
            map.put(entry.getKey(), entry.getValue());
        }
        
        List<A> as = new ArrayList<A>(mapping.keySet());
        
        TriFunction<S, A, B, Stream<S>> solutionCombiner = (s, a, b) -> Collections.<S>singleton(null).stream();
        
        KPermutationsOfNCandidateLists<A, B, S> engine =
            new KPermutationsOfNCandidateLists<>(as, map, bToClusterKey, clusterKeyToBs);
        
        Stream<ClusterStack<A, B, S>> result = engine.stream(null);
        return result; 
    }
        
    
}
