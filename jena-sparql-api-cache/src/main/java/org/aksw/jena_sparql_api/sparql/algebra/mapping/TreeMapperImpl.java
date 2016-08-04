package org.aksw.jena_sparql_api.sparql.algebra.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.aksw.combinatorics.algos.KPermutationsOfNUtils;
import org.aksw.combinatorics.collections.Cluster;
import org.aksw.combinatorics.collections.ClusterStack;
import org.aksw.commons.collections.stacks.NestedStack;
import org.aksw.jena_sparql_api.concept_cache.dirty.Tree;
import org.aksw.jena_sparql_api.concept_cache.op.TreeUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class TreeMapperImpl<A, B> {
    protected Tree<A> aTree;
    protected Tree<B> bTree;
    
    protected List<Set<A>> aTreeLevels;
    protected List<Set<B>> bTreeLevels;
    
    protected int aTreeDepth;
    protected int bTreeDepth;

    protected Multimap<A, B> baseMapping;
    
    // Function that given a mapping of parent nodes returns the matching strategy for its children  
//    protected Function<Entry<A, B>, TriFunction<
//            ? extends Collection<A>,
//            ? extends Collection<B>,
//            Multimap<A, B>,
//            Stream<Map<A, B>>>> matchingStrategy;
      protected BiFunction<A, B, ? extends MatchingStrategy<A, B>> isSatisfiable; //matchingStrategy;
    

    public TreeMapperImpl(
            Tree<A> aTree,
            Tree<B> bTree,
            Multimap<A, B> baseMapping,
            BiFunction<A, B, ? extends MatchingStrategy<A, B>> isSatisfiable 
            ) {//, Multimap<A, B> baseMapping) {
        this.aTree = aTree;
        this.bTree = bTree;

        //this.baseMapping = baseMapping; 
        this.aTreeLevels = TreeUtils.nodesPerLevel(aTree);
        this.bTreeLevels = TreeUtils.nodesPerLevel(bTree);
        
        Collections.reverse(aTreeLevels);
        Collections.reverse(bTreeLevels);

        this.aTreeDepth = aTreeLevels.size();
        this.bTreeDepth = bTreeLevels.size();
        
        //Multimap<A, B> baseMapping
        this.baseMapping = baseMapping;
        //this.matchingStrategy = matchingStrategy;
        this.isSatisfiable = isSatisfiable;
    }
    
    public static <S, X> Stream<X> stream(BiConsumer<S, Consumer<X>> fn, S baseSolution) {
        List<X> result = new ArrayList<>();
        
        fn.accept(baseSolution, (item) -> result.add(item));
        
        return result.stream();        
    }
    
    public void recurse(Multimap<A, B> parentMapping, Consumer<NestedStack<Multimap<A, B>>> consumer) {
        NestedStack<Multimap<A, B>> parentMappingStack = new NestedStack<>(null, parentMapping);
        recurse(0, parentMappingStack, consumer);
    }
    
    public void recurse(int i, NestedStack<Multimap<A, B>> parentMappingStack, Consumer<NestedStack<Multimap<A, B>>> consumer) {
        Multimap<A, B> parentMapping = parentMappingStack.getValue();
        
        if(i < aTreeLevels.size()) {            
            Set<A> keys = aTreeLevels.get(i);
            Set<B> values = bTreeLevels.get(i);
            
            Multimap<A, B> effectiveMapping = HashMultimap.create();
            
            // Nodes of the current level mapped according to the base mapping
            Multimap<A, B> levelMapping = Multimaps.filterEntries(baseMapping, new Predicate<Entry<A, B>>() {
                @Override
                public boolean apply(Entry<A, B> input) {
                    boolean result = keys.contains(input.getKey()) && values.contains(input.getValue());
                    return result;
                }            
            });
            
            effectiveMapping.putAll(levelMapping);
            effectiveMapping.putAll(parentMapping);
    
            Stream<ClusterStack<A, B, Entry<A, B>>> stream = KPermutationsOfNUtils.<A, B>kPermutationsOfN(
                    effectiveMapping,
                    aTree,
                    bTree);
    
            stream.forEach(parentClusterStack -> {

                boolean satisfiability = true;
                for(Cluster<A, B, Entry<A, B>> cluster : parentClusterStack) {
                    Entry<A, B> parentMap = cluster.getCluster();

                    MatchingStrategy<A, B> predicate = isSatisfiable.apply(parentMap.getKey(), parentMap.getValue());
                
                    Multimap<A, B> mappings = cluster.getMappings();
                    List<A> aChildren = aTree.getChildren(parentMap.getKey());
                    List<B> bChildren = bTree.getChildren(parentMap.getValue());
                                        
                    Boolean r = predicate.apply(aChildren, bChildren, mappings);
                    
                    if(!r) {
                        satisfiability = false;
                        break;
                    }
                }
                
                if(satisfiability) {
                    Multimap<A, B> nextParentMapping = HashMultimap.create();
                    for(Cluster<A, B, Entry<A, B>> cluster : parentClusterStack) {
                        Entry<A, B> e = cluster.getCluster();
                        nextParentMapping.put(e.getKey(), e.getValue());
                    }            
                    
                    NestedStack<Multimap<A, B>> nextParentMappingStack = new NestedStack<>(parentMappingStack, nextParentMapping);
                    
                    // Based on the op-types, determine the matching strategy and check whether any of the clusters has a valid mapping
    
                    // If any cluster DOES NOT have a satisfiable mapping, we can stop the recursion
                    
//                    System.out.println("GOT at level" + i + " " + nextParentMapping);
                    //System.out.println("GOT at level" + i + " " + parentClusterStack);
                    
                    
                    recurse(i + 1, nextParentMappingStack, consumer);
                }
            });
        } else {
            consumer.accept(parentMappingStack);
        }
    }
}