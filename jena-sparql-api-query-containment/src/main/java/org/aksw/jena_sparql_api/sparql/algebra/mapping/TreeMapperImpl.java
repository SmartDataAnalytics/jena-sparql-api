package org.aksw.jena_sparql_api.sparql.algebra.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.aksw.combinatorics.collections.Cluster;
import org.aksw.combinatorics.collections.ClusterStack;
import org.aksw.combinatorics.collections.NodeMapping;
import org.aksw.commons.collections.stacks.NestedStack;
import org.aksw.commons.collections.trees.Tree;
import org.aksw.commons.collections.trees.TreeUtils;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codepoetics.protonpack.functions.TriFunction;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class TreeMapperImpl<A, B, S> {
    private static final Logger logger = LoggerFactory.getLogger(TreeMapperImpl.class);

    protected Tree<A> aTree;
    protected Tree<B> bTree;

    protected List<List<A>> aTreeLevels;
    protected List<List<B>> bTreeLevels;

    protected int aTreeDepth;
    protected int bTreeDepth;

    protected Multimap<A, B> baseMapping;

    // Function that given a mapping of parent nodes returns the matching strategy for its children
//    protected Function<Entry<A, B>, TriFunction<
//            ? extends Collection<A>,
//            ? extends Collection<B>,
//            Multimap<A, B>,
//            Stream<Map<A, B>>>> matchingStrategy;
    //protected BiFunction<A, B, ? extends MatchingStrategyFactory<A, B>> isSatisfiable; //matchingStrategy;


    // TODO Maybe use a triFunction with the current stack
    //protected BiFunction<A, B, S> makeCluster;
    /**
     * Function that for a given mapping of parent nodes returns a function for
     * dealing with its children
     *
     */
    protected BiFunction<A, B, ? extends TriFunction<List<A>, List<B>, Multimap<A, B>, S>> matchingStrategyFactory;

    protected Predicate<S> isSatisfiable;

    public TreeMapperImpl(
            Tree<A> aTree,
            Tree<B> bTree,
            Multimap<A, B> baseMapping,
            //BiFunction<A, B, ? extends MatchingStrategyFactory<A, B>> isSatisfiable
            //BiFunction<A, B, S> makeCluster,
            BiFunction<A, B, ? extends TriFunction<List<A>, List<B>, Multimap<A, B>, S>> matchingStrategyFactory,
            Predicate<S> isSatisfiable

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
        this.matchingStrategyFactory = matchingStrategyFactory;
        this.isSatisfiable = isSatisfiable;
    }



//    public static <S, X> Stream<X> stream(Consumer<Consumer<X>> fn, S ) {
//        List<X> result = new ArrayList<>();
//
//        fn.accept(baseSolution, (item) -> result.add(item));
//
//        return result.stream();
//    }


    public void recurse(Map<A, B> parentMapping, Consumer<NestedStack<LayerMapping<A, B, S>>> consumer) {
        LayerMapping<A, B, S> layerMapping = new LayerMapping<>(Collections.emptyList(), parentMapping);

        //NestedStack<LayerMapping<A, B, S>> parentMappingStack = new NestedStack<>(null, parentMapping);
        NestedStack<LayerMapping<A, B, S>> parentMappingStack = new NestedStack<>(null, layerMapping);
        recurse(0, parentMappingStack, consumer);
    }


    public void recurse(int i, NestedStack<LayerMapping<A, B, S>> parentLayerMappingStack, Consumer<NestedStack<LayerMapping<A, B, S>>> consumer) {

        if(logger.isDebugEnabled()) {
            logger.debug("Entering level " + i);
        }


        //Map<A, B> parentMapping = parentMappingStack.getValue();
        Map<A, B> parentMapping = parentLayerMappingStack.getValue().getParentMap();

        if(i < aTreeLevels.size()) {
            Set<A> keys = Sets.newIdentityHashSet();
            keys.addAll(aTreeLevels.get(i));

            Set<B> values = Sets.newIdentityHashSet();
            values.addAll(bTreeLevels.get(i));


            Multimap<A, B> effectiveMapping = HashMultimap.create();

            // *Only* use the nodes of the current level mapped according to the base mapping
            Multimap<A, B> levelMapping = Multimaps.filterEntries(baseMapping, new com.google.common.base.Predicate<Entry<A, B>>() {
                @Override
                public boolean apply(Entry<A, B> input) {
                    boolean result = keys.contains(input.getKey()) && values.contains(input.getValue());
                    return result;
                }
            });

            // In addition to the candidate mappings of the current level, also add those that may have been introduced
            // by the parent mapping

            effectiveMapping.putAll(levelMapping);
            //effectiveMapping.putAll(parentMapping);
            //effectiveMapping.entries().addAll(parentMapping.entrySet());
            parentMapping.entrySet().forEach(e -> effectiveMapping.put(e.getKey(), e.getValue()));


            // Each cluster stack represents potential mappings of all of this level's parent nodes
            Stream<ClusterStack<A, B, Entry<A, B>>> stream = TreeMapperImpl.<A, B>kPermutationsOfN(
                    effectiveMapping,
                    aTree,
                    bTree);

            stream.forEach(parentClusterStack -> {

                boolean satisfiability = true;
                //LayerMapping<A, B, S> layerMapping = new LayerMapping<>();
                List<NodeMapping<A, B, S>> nodeMappings = new ArrayList<>();

                for(Cluster<A, B, Entry<A, B>> cluster : parentClusterStack) {
                    Entry<A, B> parentMap = cluster.getCluster();

                    A aParent = parentMap.getKey();
                    B bParent = parentMap.getValue();



                    // Obtain the matching strategy function for the given parents
                    TriFunction<List<A>, List<B>, Multimap<A, B>, S> matchingStrategy = matchingStrategyFactory.apply(aParent, bParent);
                    //boolean r = isSatisfiable.apply(clusterX);
                    //MatchingStrategyFactory<A, B> predicate = isSatisfiable.apply(parentMap.getKey(), parentMap.getValue());

                    Multimap<A, B> mappings = cluster.getMappings();
                    List<A> aChildren = new ArrayList<>(aTree.getChildren(parentMap.getKey()));
                    List<B> bChildren = new ArrayList<>(bTree.getChildren(parentMap.getValue()));

                    S clusterX = matchingStrategy.apply(aChildren, bChildren, mappings);
                    boolean r = isSatisfiable.test(clusterX);

                    NodeMapping<A, B, S> nodeMapping = new NodeMapping<>(aTree, bTree, parentMap, effectiveMapping, clusterX);
                    nodeMappings.add(nodeMapping);

//
//                    IterableUnknownSize<Map<A, B>> it = predicate.apply(aChildren, bChildren, mappings);
//                    boolean r = it.mayHaveItems();

                    if(logger.isDebugEnabled()) {
                        logger.debug("  Source: " + aParent);
                        logger.debug("  Target: " + bParent);
                        logger.debug("  Satisfiable: " + satisfiability);
                        nodeMappings.forEach(m ->
                            m.getChildMapping().entries().forEach(n ->
                                logger.debug("  ChildMapping: " + n)));
                    }

                    if(!r) {
                        satisfiability = false;
                        break;
                    }
                }

                if(satisfiability) {
                    //Multimap<A, B> nextParentMapping = HashMultimap.create();
                    Map<A, B> nextParentMapping = new IdentityHashMap<>();
                    for(Cluster<A, B, Entry<A, B>> cluster : parentClusterStack) {
                        Entry<A, B> e = cluster.getCluster();
                        nextParentMapping.put(e.getKey(), e.getValue());
                    }

                    LayerMapping<A, B, S> layerMapping = new LayerMapping<>(nodeMappings, nextParentMapping);

                    //MultiClusterStack<A, B, Entry<A, B>> nextMcs = new MultiClusterStack<>(null, parentClusterStack);


                    //NestedStack<Map<A, B>> nextParentMappingStack = new NestedStack<>(parentMappingStack, nextParentMapping);


                    NestedStack<LayerMapping<A, B, S>> nextLayerMappingStack = new NestedStack<>(parentLayerMappingStack, layerMapping);

                    // Based on the op-types, determine the matching strategy and check whether any of the clusters has a valid mapping

                    // If any cluster DOES NOT have a satisfiable mapping, we can stop the recursion

//                    System.out.println("GOT at level" + i + " " + nextParentMapping);
                    //System.out.println("GOT at level" + i + " " + parentClusterStack);


                    recurse(i + 1, nextLayerMappingStack, consumer);
                }
            });
        } else {
            consumer.accept(parentLayerMappingStack);
        }
    }
    
    
    
    public static <A, B> Stream<ClusterStack<A, B, Entry<A, B>>> kPermutationsOfN(
            Multimap<A, B> childMapping,
            Tree<A> aTree,
            Tree<B> bTree) {

        Multimap<A, B> parentMapping = TreeUtils.deriveParentMapping(aTree, bTree, childMapping);
        List<A> as = new ArrayList<>(parentMapping.keySet());

        //TriFunction<S, A, B, Stream<S>> solutionCombiner = (s, a, b) -> Collections.<S>singleton(null).stream();
        
        TreeMapperCandidateList<A, B> engine =
            new TreeMapperCandidateList<>(aTree, bTree, as, childMapping, parentMapping);
        
        Stream<ClusterStack<A, B, Entry<A, B>>> result = engine.stream();
        return result;
    }
}