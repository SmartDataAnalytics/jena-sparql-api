package org.aksw.jena_sparql_api.deprecated.iso.index;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Stream;

import org.aksw.combinatorics.solvers.ProblemNeighborhoodAware;
import org.aksw.commons.collections.utils.StreamUtils;
import org.aksw.jena_sparql_api.concept_cache.combinatorics.ProblemMappingKPermutationsOfN;
import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphMapping;
import org.jgrapht.alg.isomorphism.IsomorphicGraphMapping;
import org.jgrapht.alg.isomorphism.VF2SubgraphIsomorphismInspector;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class ProblemNodeMappingGraph<V, E, T>
    implements ProblemNeighborhoodAware<BiMap<V, V>, T>
    //extends ProblemMappingVarsBase<DirectedGraph<Node, Triple>, DirectedGraph<Node, Triple>, Var, Var>
{
    protected DirectedGraph<V, E> viewGraph;
    protected DirectedGraph<V, E> queryGraph;
    protected Comparator<V> nodeComparator;
    protected Comparator<E> edgeComparator;

    protected BiMap<V, V> baseSolution;

    protected VF2SubgraphIsomorphismInspector<V, E> inspector;

    protected Function<BiMap<V, V>, Comparator<V>> nodeComparatorFactory;
    protected Function<BiMap<V, V>, Comparator<E>> edgeComparatorFactory;

    public ProblemNodeMappingGraph(
            BiMap<V, V> baseSolution,
            DirectedGraph<V, E> viewGraph,
            DirectedGraph<V, E> queryGraph,
            Function<BiMap<V, V>, Comparator<V>> nodeComparatorFactory,
            Function<BiMap<V, V>, Comparator<E>> edgeComparatorFactory) {

        this.baseSolution = baseSolution;
        this.viewGraph = viewGraph;
        this.queryGraph = queryGraph;

        nodeComparator = nodeComparatorFactory.apply(baseSolution);
        edgeComparator = edgeComparatorFactory.apply(baseSolution);
        inspector = new VF2SubgraphIsomorphismInspector<>(queryGraph, viewGraph, nodeComparator, edgeComparator, true);

        //super(as, bs, baseSolution);
    }

    @Override
    public Stream<BiMap<V, V>> generateSolutions() {
        Iterator<GraphMapping<V, E>> it = inspector.getMappings();

        Stream<BiMap<V, V>> result = StreamUtils.stream(it)
            .map(m -> (IsomorphicGraphMapping<V, E>)m)
            .map(m -> {
                BiMap<V, V> nodeMap = HashBiMap.create();//new HashMap<>();

                // Add the base solution
                nodeMap.putAll(baseSolution);

                for(V bNode : queryGraph.vertexSet()) {
                    if(m.hasVertexCorrespondence(bNode)) {
                        V aNode = m.getVertexCorrespondence(bNode, true);
                            nodeMap.put(aNode, bNode);
                    }
                }
                return nodeMap;
            });

        return result;
    }

    @Override
    public Collection<? extends ProblemNeighborhoodAware<BiMap<V, V>, T>> refine(BiMap<V, V> partialSolution) {
        BiMap<V, V> newBaseSolution = HashBiMap.create(baseSolution.size() + partialSolution.size());
        newBaseSolution.putAll(baseSolution);
        newBaseSolution.putAll(partialSolution);

        return Collections.singleton(new ProblemNodeMappingGraph<>(newBaseSolution, viewGraph, queryGraph, nodeComparatorFactory, edgeComparatorFactory));
    }

    @Override
    public boolean isEmpty() {
        boolean result = inspector.isomorphismExists();
        return result;
    }

    @Override
    public long getEstimatedCost() {
        // n over k based on the number of edges
        // The more the graphs differ in size, the higher the cost
        int n = queryGraph.edgeSet().size();
        int k = viewGraph.edgeSet().size();

        long result = ProblemMappingKPermutationsOfN.kCombinationCount(n, k);
        System.out.println("estimated cost: " + result);
        return result;
    }

    @Override
    public Collection<T> getSourceNeighbourhood() {
        return null;
    }

}
