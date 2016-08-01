package org.aksw.combinatorics.algos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.combinatorics.collections.ClusterStack;
import org.aksw.combinatorics.collections.CombinationStack;
import org.aksw.commons.collections.multimaps.BiHashMultimap;
import org.aksw.jena_sparql_api.concept_cache.dirty.Tree;
import org.aksw.jena_sparql_api.concept_cache.op.TreeUtils;
import org.aksw.mapping.TreeMapperCandidateList;

import com.codepoetics.protonpack.functions.TriFunction;
import com.google.common.collect.HashMultimap;
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
    
    // TODO How to handle root nodes / null values?
    public static <A, B> Multimap<A, B> deriveParentMapping(Tree<A> aTree, Tree<B> bTree, Multimap<A, B> childMapping) {
        Multimap<A, B> result = HashMultimap.create();
        Set<A> as = childMapping.keySet();
        for(A a : as) {
            A aParent = aTree.getParent(a);
            Collection<B> bs = childMapping.get(a);
            Set<B> bParents = TreeUtils.getParentsOf(bTree, bs);

            result.putAll(aParent, bParents);
        }

        return result;
    }
    
    
    public static <A, B> Stream<ClusterStack<A, B, Entry<A, B>>> kPermutationsOfN(
            Multimap<A, B> childMapping,
            Tree<A> aTree,
            Tree<B> bTree) {

        Multimap<A, B> parentMapping = deriveParentMapping(aTree, bTree, childMapping);
        List<A> as = new ArrayList<>(parentMapping.keySet());

        //TriFunction<S, A, B, Stream<S>> solutionCombiner = (s, a, b) -> Collections.<S>singleton(null).stream();
        
        TreeMapperCandidateList<A, B> engine =
            new TreeMapperCandidateList<>(aTree, bTree, as, childMapping, parentMapping);
        
        Stream<ClusterStack<A, B, Entry<A, B>>> result = engine.stream();
        return result; 
    }
    

    //public static <A, B> Stream<CombinationStack<A, B, Object>> kPermutationsOfN(Multimap<A, B> mapping) {
    public static <A, B> Stream<Map<A, B>> kPermutationsOfN(Multimap<A, B> mapping) {
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
            Map<A, B> r = stack.stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue));
            return r;
        });
        
        return res; 
    }
        
    
}
