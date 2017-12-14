package org.aksw.jena_sparql_api.iso.index2;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.aksw.commons.collections.reversible.ReversibleMap;
import org.aksw.commons.collections.reversible.ReversibleMapImpl;
import org.aksw.commons.graph.index.core.SubgraphIsomorphismIndex;
import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphMapping;
import org.jgrapht.Graphs;
import org.jgrapht.alg.isomorphism.IsomorphicGraphMapping;
import org.jgrapht.alg.isomorphism.VF2SubgraphIsomorphismInspector;
import org.jgrapht.graph.SimpleDirectedGraph;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Streams;

/**
 * Implementation of the sub graph isomorphism index which merges all insert DAGs (TODO would it also work with arbitrary graphs)
 * into a global graph.
 *
 * TODO: Insert of a graph may reveal that it is isomorphic to multiple graphs - i.e. it is a common subgraph of other graphhs
 *
 *
 *
 * @author raven
 *
 */
public abstract class SubGraphIsomorphismIndex2<K, N, E>
    implements SubgraphIsomorphismIndex<K, DirectedGraph<N, E>, N>
{
    protected Class<E> edgeClass;
    protected DirectedGraph<N, E> globalGraph;

    //protected ReversibleSetMultimap<DirectedGraph<N, E>, K> graphToKeys;


    protected ReversibleMap<K, DirectedGraph<N, E>> keyToSubGraph = new ReversibleMapImpl<>();
    protected ReversibleMap<K, N> keyToRootNode = new ReversibleMapImpl<>();

    public abstract Comparator<N> createNodeComparator(BiMap<N, N> baseIso);
    public abstract Comparator<E> createEdgeComparator(BiMap<N, N> baseIso);


    //protected ReversibleMap<K, G> keyToRootNode;


//     public static Stream<BiMap<Node, Node>> match(
//    BiMap<Node, Node> baseIso, // view to user query
//    DirectedGraph<Node, Triple> a,
//    DirectedGraph<Node, Triple> b) {

    public Iterable<BiMap<N, N>> match(BiMap<N, N> baseIso, DirectedGraph<N, E> a, DirectedGraph<N, E> b) {
        Comparator<N> nodeCmp = createNodeComparator(baseIso);
        Comparator<E> edgeCmp = createEdgeComparator(baseIso);

        VF2SubgraphIsomorphismInspector<N, E> inspector =
                new VF2SubgraphIsomorphismInspector<>(b, a, nodeCmp, edgeCmp, true);
        Iterator<GraphMapping<N, E>> it = inspector.getMappings();

        Stream<BiMap<N, N>> r = Streams.stream(it)
            .map(m -> (IsomorphicGraphMapping<N, E>)m)
            .map(m -> { // TODO Maybe implementing a BiMap wrapper backed by IsomorphicGraphMapping improves performance a little
                BiMap<N, N> nodeMap = HashBiMap.create();//new HashMap<>();
                for(N bNode : b.vertexSet()) {
                    if(m.hasVertexCorrespondence(bNode)) {
                        N aNode = m.getVertexCorrespondence(bNode, true);
                        nodeMap.put(aNode, bNode);
                    }
                }

                return nodeMap;
            });

        // Todo use memoizing iterable
        Iterable<BiMap<N, N>> result = () -> r.iterator();

        return result;
    }

    public DirectedGraph<N, E> createSet() {
        return new SimpleDirectedGraph<>(edgeClass);
    }


    public abstract E applyIso(BiMap<N, N> iso, E edge);


    public int size(DirectedGraph<N, E> set) {
        return set.edgeSet().size();
    }


    /**
     *
     * @param baseSet
     * @return For each key the isomorphisms under which there are sub graphs
     */
    public Multimap<K, BiMap<N, N>> findSubGraphs(DirectedGraph<N, E> baseGraph) {
        N root = expectOneItem(findRoots(baseGraph));

        Iterable<BiMap<N, N>> isos = match(HashBiMap.create(), baseGraph, globalGraph);

        Multimap<K, BiMap<N, N>> result = ArrayListMultimap.create();

        for(BiMap<N, N> iso : isos) {
            N isoRoot = iso.get(root);

            Stream<N> keys = reachableNodes(
                        isoRoot,
                        v -> Graphs.predecessorListOf(globalGraph, v).stream(),
                        v -> !keyToRootNode.reverse().get(v).isEmpty()
                    )
                    .map(Entry::getKey);


            keys.forEach(n -> keyToRootNode.reverse().get(n).forEach(k -> result.put(k, iso)));
        }

        return result;
    }

    /**
     * Recursively navigate from a start point
     *
     * @param nav
     * @param stopCondition
     * @return
     */
    public static <T> Stream<Entry<T, Long>> reachableNodes(T start, Function<T, Stream<T>> nav, Predicate<T> stopCondition) {
        Set<T> visited = new HashSet<>();

        Stream<Entry<T, Long>> result = reachableNodes(0l, visited, start, nav, stopCondition);
        return result;
    }

    public static <T> Stream<Entry<T, Long>> reachableNodes(long depth, Set<T> visited, T start, Function<T, Stream<T>> nav, Predicate<T> stopCondition) {
        visited.add(start);

        Stream<Entry<T, Long>> result = stopCondition.test(start)
                ? Stream.of(new SimpleEntry<>(start, depth))
                : nav.apply(start).filter(v -> !visited.contains(v)).flatMap(v -> reachableNodes(depth + 1, visited, v, nav, stopCondition));
        return result;
    }

    public static <T> T expectOneItem(Stream<T> stream) {
        T result = null;
        Iterator<T> it = stream.iterator();
        if(!it.hasNext()) {
            throw new RuntimeException("Exactly one item expected in stream; got none");
        }

        result = it.next();

        if(it.hasNext()) {
            stream.close();
            throw new RuntimeException("Exactly one item expected in stream; got multiple");
        }

        return result;
    }

    public static <V, E> Stream<V> findRoots(DirectedGraph<V, E> graph) {
        Stream<V> result = graph.vertexSet().stream().filter(v -> graph.incomingEdgesOf(v).isEmpty());
        return result;
    }



    @Override
    public void removeKey(Object key) {

        //DirectedAcyclicGraph<V, E>


        // TODO Auto-generated method stub

    }

    @Override
    public K put(K key, DirectedGraph<N, E> insertGraph) {
        Iterable<BiMap<N, N>> isos = match(HashBiMap.create(), insertGraph, globalGraph);

        // For each found isomorphism:
        // Check if application of the iso is equivalent to an existing graph in the map.

        boolean foundAtLeastOneIso = false;
        for(BiMap<N, N> iso : isos) {
            foundAtLeastOneIso = true;

            DirectedGraph<N, E> isoGraph = null; // applyIso(iso, insertGraph);

            // If there are no keys, we need to insert the graph
            Collection<K> keys = keyToSubGraph.reverse().get(isoGraph);
            keys.add(key);
        }

//        if(!foundAtLeastOneIso) {
//            // If the insert graph is larger (has more nodes and more edges), check if the global graph is isomorph to it
//            Iterable<BiMap<N, N>> isos = match(HashBiMap.create(), globalGraph, insertGraph);
//
//
//            Graphs.addGraph(globalGraph, insertGraph);
//            graphToKeys.put(insertGraph, key);
//        }

        // If the insert graph is larger than the global graph, check if this one is a subset

        // Find isomorphism
        Graphs.addGraph(globalGraph, insertGraph);


        return key;
    }

}
