package org.aksw.jena_sparql_api.iso.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collections.MapUtils;
import org.aksw.commons.collections.set_trie.TagMap;
import org.aksw.commons.collections.set_trie.TagMapSetTrie;
import org.apache.jena.atlas.io.IndentedWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clearspring.analytics.util.AbstractIterator;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import com.google.common.collect.Table;
import com.google.common.io.ByteStreams;

/**
 * Generic sub graph isomorphism index class that on the surface acts like a Map.
 * The ways the index can be seen are:
 * <ul>
 * <li>Keys of type K are associated with a graph G with nodes type V.</li>
 * <li>Keys of type K are associated with a set G whose items are composed from atoms of type V.</li>
 *   For instance, an RDF graph (V = org.apache.jena.graph.Graph) is a set of triples,
 *   which are composed of V=org.apache.jena.graph.Node nodes.
 * <ul>
 *
 * Type T is for tags, which is a set of static features of G.
 * Typically, this is a set of constants in G;
 * which is invariant - i.e. never remapped - by an isomorphism.
 *
 *
 *
 * @author raven
 *
 */
public class SubGraphIsomorphismIndexImpl<K, G, V, T>
    implements SubGraphIsomorphismIndex<K, G, V>
{
    private static final Logger logger = LoggerFactory.getLogger(SubGraphIsomorphismIndexImpl.class);

    /*
     * Customizable fields
     */

    protected IsoMatcher<G, V> isoMatcher;
    protected Function<? super G, Collection<T>> extractGraphTags;
    protected Comparator<? super T> tagComparator;
    protected SetOps<G, V> setOps;


    /*
     * Internal fields
     */
//    protected DirectedGraph<K, Edge<K, G, V, T>> indexGraph = new SimpleDirectedGraph<>(Edge.class);

    protected IndexNode<K, G, V, T> rootNode;
    protected Map<K, IndexNode<K, G, V, T>> keyToNode = new HashMap<>();

    /**
     * This table maps preferred keys to their alternate keys and the isos from pref keys' graph of that of alt
     * So we can transition from a pref key to all other isomorphic graphs
     */
    protected Table<K, K, BiMap<V, V>> prefKeyToAltKeysWithIso = HashBasedTable.create();


    protected boolean enableDebugInfo = false;
    protected IndentedWriter writer = enableDebugInfo
            ? IndentedWriter.stderr
            :  new IndentedWriter(ByteStreams.nullOutputStream());


    public SubGraphIsomorphismIndexImpl(
            SetOps<G, V> setOps,
            Function<? super G, Collection<T>> extractGraphTags,
            Comparator<? super T> tagComparator,
            IsoMatcher<G, V> isoMatcher
            ) {
        super();
        this.setOps = setOps;
        this.extractGraphTags = extractGraphTags;
        this.tagComparator = tagComparator;
        this.isoMatcher = isoMatcher;

        rootNode = createNode(null, setOps.createNew(), Collections.emptySet());
        keyToNode.put(null, rootNode);
    }

    protected Set<T> extractGraphTagsWrapper(G graph) {
        Collection<T> tmp = extractGraphTags.apply(graph);
        Set<T> result = tmp.stream().collect(Collectors.toSet());
        return result;
    }


    /* (non-Javadoc)
     * @see org.aksw.jena_sparql_api.iso.index.SubGraphIsomorphismIndex#removeKey(java.lang.Object)
     */
    @Override
    public void removeKey(Object key) {

        // FIXME Make the cast more safe - we should never throw an exception
        K prefKey = getPrefKey((K)key);
        Set<K> altKeys = prefKeyToAltKeysWithIso.row(prefKey).keySet();

        // Remove the key from the alt keys
        altKeys.remove(key);

        // If the prefKey no longer has any alt keys, remove all nodes associated with that key
        boolean extinguishNodes = altKeys.isEmpty();

        if(extinguishNodes) {
            IndexNode<K, G, V, T> node = keyToNode.get(prefKey);
            extinguishNode(node);
        }
    }


    /**
     * Create a node with an fresh internal id
     *
     * @param graphIso
     * @return
     */
    protected IndexNode<K, G, V, T> createNode(K key, G graph, Set<T> graphTags) {
        TagMap<Edge<K, G, V, T>, T> tagMap = new TagMapSetTrie<>(tagComparator);
        IndexNode<K, G, V, T> result = new IndexNode<>(key, graph, graphTags, tagMap);
        keyToNode.put(key, result);

        return result;
    }


    public Multimap<K, BiMap<V, V>> lookupX(G queryGraph, boolean exactMatch) {
        Multimap<K, InsertPosition<K, G, V, T>> matches = lookup(queryGraph, exactMatch);

        Multimap<K, BiMap<V, V>> result = HashMultimap.create();
        //matches.values().map()
//        breadthFirstSearchWithMultipleStartNodesAndOrderedChildren(
//            nodes,
//            node -> prefKeyToNode.reversed().get(node.getId()),
//            prefKeyToGraph,
//            graphToSize, // Create the associated graph, then takes it size
//            node ->
//            node -> Stream.ofNullable(node.getParent()));


        for(Entry<K, Collection<InsertPosition<K, G, V, T>>> match : matches.asMap().entrySet()) {
            K prefKey = match.getKey();

            Map<K, BiMap<V, V>> altKeys = prefKey == null ? Collections.emptyMap() : prefKeyToAltKeysWithIso.row(prefKey);

            for(InsertPosition<K, G, V, T> pos : match.getValue()) {
                BiMap<V, V> baseIso = pos.getIso();

                for(Entry<K, BiMap<V, V>> e : altKeys.entrySet()) {

                    K altKey = e.getKey();
                    //BiMap<V, V> kIso = baseIso;
                    BiMap<V, V> transIso = e.getValue();
                    //transIso = transIso.inverse();
                    // TODO THe transIso is just the delta - we need to assemble it from all parents
                    //System.out.println("Iso from " + prefKey + " to " + altKey + ": " + transIso);
                    BiMap<V, V> altKeyIso = mapDomainVia(baseIso, transIso);
                    altKeyIso = removeIdentity(altKeyIso);
                    //kIso = removeIdentity(kIso);
                    result.put(altKey, altKeyIso);
                }
            }
        }

        return result;
    }


    /* (non-Javadoc)
     * @see org.aksw.jena_sparql_api.iso.index.SubGraphIsomorphismIndex#lookup(G, boolean)
     */
    //@Override
    public Multimap<K, InsertPosition<K, G, V, T>> lookup(G queryGraph, boolean exactMatch) {

        Set<T> queryGraphTags = extractGraphTagsWrapper(queryGraph);

        Collection<InsertPosition<K, G, V, T>> positions = new LinkedList<>();
        findInsertPositions(positions, rootNode, queryGraph, queryGraphTags, HashBiMap.create(), HashBiMap.create(), true, exactMatch, writer);

        Multimap<K, InsertPosition<K, G, V, T>> result = HashMultimap.create();
        logger.debug("Lookup result candidates: " + positions.size());
        for(InsertPosition<K, G, V, T> pos : positions) {
            // Match with the children

            result.put(pos.getNode().getKey(), pos);
            //System.out.println("Node " + pos.node + " with keys " + pos.node.getKeys() + " iso: " + pos.getGraphIso().getInToOut());
//            for(K key : pos.node.getKeys()) {
//                result.put(key, pos);
//            }
        }
        return result;
    }





    /**
     * Lookup only pref keys / this skips results isomorphic to the given keys
     * @param queryGraph
     * @param exactMatch
     * @return
     */
    public Multimap<K, BiMap<V, V>> lookupFlat(G queryGraph, boolean exactMatch) {

        Set<T> queryGraphTags = extractGraphTagsWrapper(queryGraph);


        Collection<InsertPosition<K, G, V, T>> positions = new LinkedList<>();

        findInsertPositions(positions, rootNode, queryGraph, queryGraphTags, HashBiMap.create(), HashBiMap.create(), true, exactMatch, writer);

        Multimap<K, BiMap<V, V>> result = HashMultimap.create();

        if(logger.isDebugEnabled()) {
            logger.debug("Lookup result candidates: " + positions.size());
        }

        for(InsertPosition<K, G, V, T> pos : positions) {
            // Match with the children

            result.put(pos.getNode().getKey(), pos.getIso());
            //System.out.println("Node " + pos.node + " with keys " + pos.node.getKeys() + " iso: " + pos.getGraphIso().getInToOut());
//            for(K key : pos.node.getKeys()) {
//                result.put(key, pos.getIso());
//            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.aksw.jena_sparql_api.iso.index.SubGraphIsomorphismIndex#put(K, G)
     */
    @Override
    public K put(K key, G graph) {
        Set<T> insertGraphTags = extractGraphTagsWrapper(graph);

        add(rootNode, key, graph, insertGraphTags, HashBiMap.create(), false, writer);

        return null;
    }


    public static <T> Iterable<T> toIterable(Stream<T> stream) {
        Iterable<T> result = () -> stream.iterator();
        return result;
    }

//    /**
//     * Return the subset of BC not covered
//     *
//     * @return
//     */
//    public static BiMap<T, T> isoDiff(isoCD, isoBC) {
//
//    }


    public boolean isEmpty(G graph) {
        boolean result = setOps.size(graph) == 0;
        return result;
    }


    /**
     * Transitively map all elements in the domain of 'src'
     * { x -> z | x in dom(src) & z = via(src(x)) }
     *
     * FIXME Return a BiMap view instead of a materialized copy
     *
     * @param src
     * @param map
     * @return
     */
    public static <N> BiMap<N, N> mapDomainVia(Map<N, N> src, Map<N, N> map) {
        BiMap<N, N> result = src.entrySet().stream().collect(Collectors.toMap(
                e -> map.getOrDefault(e.getKey(), e.getKey()),
                e -> e.getValue(),
                (u, v) -> {
                    throw new RuntimeException("should not hapen: " + src + " --- map: " + map);
                },
                HashBiMap::create));
        return result;
    }

    public static <T> BiMap<T, T> removeIdentity(Map<T, T> map) {
        BiMap<T, T> result = map.entrySet().stream()
                .filter(e -> !Objects.equals(e.getKey(), e.getValue()))
                .collect(collectToBiMap(Entry::getKey, Entry::getValue));
        return result;
    }

    public static <T, K, U> Collector<T, ?, BiMap<K, U>> collectToBiMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
        Collector<T, ?, BiMap<K, U>> x = Collectors.toMap(
                keyMapper, valueMapper,
                (u, v) -> {
                    throw new RuntimeException("should not hapen: " + u + " --- map: " + v);
                },
                HashBiMap::create);
        return x;
    }


    public static <X, Y> BiMap<X, X> chain(Map<X, Y> src, Map<? super Y, X> map) {
        BiMap<X, X> result = HashBiMap.create();
        for(Entry<X, Y> e : src.entrySet()) {
            X k = e.getKey();
            Y l = e.getValue();
            X m = map.get(l);
            if(m != null) {
                //System.out.println("Put: " + k + " -> " + m);
                result.put(k, m);
            }
        }
        return result;
    }


    /**
     * For a given insertGraph, find all nodes in the tree at which insert has to occurr.
     *
     *
     * @param out
     * @param node
     * @param insertGraph
     * @param baseIso
     * @param retrievalMode false: only return leaf nodes of insertion, true: return all encountered nodes
     * @param writer
     */
    void findInsertPositions(Collection<InsertPosition<K, G, V, T>> out, IndexNode<K, G, V, T> node, G insertGraph, Set<T> insertGraphTags, BiMap<V, V> baseIso, BiMap<V, V> latestIsoAB, boolean retrievalMode, boolean exactMatch, IndentedWriter writer) {

        writer.println("Finding insert position for user graph of size " + setOps.size(insertGraph));

        // Create the residual set of tags by removing the tags present on the current node from the graphTags

        boolean isSubsumed = false;

        boolean insertAtThisNode = false;



        // Candidate children for recursive lookup of the insert position
        // are those whose tags are subsets of the insertGraphTags
        writer.incIndent();
        Collection<Edge<K, G, V, T>> candEdges =
                node.getEdgeIndex().getAllSubsetsOf(insertGraphTags, false).keySet();

        for(Edge<K, G, V, T> candEdge : candEdges) {
            G viewGraph = candEdge.getResidualGraph();

            writer.println("Comparison with view graph of size " + setOps.size(viewGraph));

            // For every found isomorphism, check all children whether they are also isomorphic.
            writer.incIndent();
            int i = 0;

            // Before testing for isomorphy,
            // we need to remap the baseIso with the candidate node's transIso
            // (transIso captures the remapping of set/graph items when moving from the parent to the child node)
            // E.g. if the parent was {(?x a Foo)} and the child is {(?s a Bar)}, then the transIso could be ?x -> ?s
            // if the child node's full graph was { ?s a Foo ; a Bar }.
            BiMap<V, V> childTransIso = candEdge.getTransIso();
            BiMap<V, V> transBaseIso = mapDomainVia(baseIso, childTransIso);

            Iterable<BiMap<V, V>> isos = isoMatcher.match(transBaseIso, viewGraph, insertGraph);

            for(BiMap<V, V> iso : isos) {
                // The difference is all non-identical mappings
                // FIXME We could exclude identical mappings already in the iso matcher
                BiMap<V, V> deltaIso = removeIdentity(iso);

                writer.println("Found match #" + ++i + ":");
                writer.incIndent();

                // We need to validate whether the mapping is compatible with the base mapping
                // E.g. if we insert [i1: { ?s ?p ?o }, i2: { ?x a Person }, i3: { ?y a Person ; label ?l}
                // Then there will be two isos from i1 to i3, but only one that is compatible with i2
                boolean isCompatible = MapUtils.isCompatible(iso, transBaseIso);
                if(!isCompatible) {
                    writer.println("Incompatible:");
                    writer.println("iso         : " + iso);
                    writer.println("transBaseIso: " + transBaseIso);
                    continue;
                }

                // A graph is only subsumed if the found iso is compatible with the base iso
                isSubsumed = true;

                // Affected keys are the nodes of the view graph that were newly mapped by the iso
                // We implement a state-space-search approach here: We update the transBaseIso in place
                Set<V> affectedKeys = new HashSet<>(Sets.difference(iso.keySet(), transBaseIso.keySet()));


                writer.println("affectedkeys: " + affectedKeys);
                writer.println("iso         : " + iso);
                writer.println("deltaIso    : " + deltaIso);

                affectedKeys.forEach(k -> transBaseIso.put(k, iso.get(k)));

                G g = setOps.applyIso(viewGraph, iso);

                G residualInsertGraph = setOps.difference(insertGraph, g);

                // now create the diff between the insert graph and mapped child graph
                writer.println("Diff " + residualInsertGraph + " has "+ setOps.size(residualInsertGraph) + " triples at depth " + writer.getUnitIndent());

                // TODO optimize handling of empty diffs
                K childNodeKey = candEdge.getTo();
                IndexNode<K, G, V, T> childNode = keyToNode.get(childNodeKey);

                Set<T> residualInsertGraphTags = Sets.difference(insertGraphTags, candEdge.getResidualGraphTags());

                findInsertPositions(out, childNode, residualInsertGraph, residualInsertGraphTags, transBaseIso, deltaIso, retrievalMode, exactMatch, writer);

                affectedKeys.forEach(transBaseIso::remove);

                writer.decIndent();
            }
            writer.decIndent();
        }
        writer.decIndent();


        insertAtThisNode = insertAtThisNode || !isSubsumed;
        if(insertAtThisNode || retrievalMode) {
        //if(!isSubsumed || retrievalMode) {

            if(!exactMatch || isEmpty(insertGraph)) {
                writer.println("Marking location for insert");
                //System.out.println("keys at node: " + node.getKeys() + " - " + node);
                // Make a copy of the baseIso, as it is transient due to state space search
                InsertPosition<K, G, V, T> pos = new InsertPosition<>(node, insertGraph, insertGraphTags, HashBiMap.create(baseIso), latestIsoAB);
                out.add(pos);
            }
        }
    }



    /**
     * Test whether the node is a leaf without any associated keys
     *
     * @param node
     * @return
     */
    protected boolean isEmptyLeafNode(IndexNode<K, G, V, T> node) {
        K prefKey = node.getKey();
        Set<K> altKeys = prefKey == null ? Collections.emptySet() : prefKeyToAltKeysWithIso.row(prefKey).keySet();

        boolean result =
                node.isLeaf() && altKeys.isEmpty();

        return result;
    }

//    protected void extinguishNode(long nodeId) {
//        GraphIndexNode<K, G, V, T> node = idToNode.get(nodeId);
//        extinguishNode(node);
//    }

    protected void extinguishNode(IndexNode<K, G, V, T> node) {
        if(node != null && node.getKey() != null) { // Do not extinguish the root node
            // If the node is a child node, then remove it
            if(isEmptyLeafNode(node)) {
                deleteNode(node);

                for(K parentNodeKey : node.getParents()) {
                    IndexNode<K, G, V, T> parentNode = keyToNode.get(parentNodeKey);
                    extinguishNode(parentNode);
                }
            }
        }
    }

    //@Override
    public void deleteNode(IndexNode<K, G, V, T> node) {
        K nodeKey = node.getKey();

        for(K parentNodeKey : node.getParents()) {
            IndexNode<K, G, V, T> parentNode = keyToNode.get(parentNodeKey);

            parentNode.removeChildById(nodeKey);
        }

        keyToNode.remove(nodeKey);
    }

    /**
     * During the insert procedure, the insert graph is never renamed, because we want to figure out
     * how to remap existing nodes such they become a subgraph of the insertGraph.
     *
     * @param graph
     */
    void add(IndexNode<K, G, V, T> node, K key, G insertGraph, Set<T> insertGraphTags, BiMap<V, V> baseIso, boolean forceInsert, IndentedWriter writer) {
        // The insert graph must be larger than the node Graph


        Collection<InsertPosition<K, G, V, T>> positions = new LinkedList<>();
        HashBiMap<V, V> deltaIso = HashBiMap.create();
        findInsertPositions(positions, node, insertGraph, insertGraphTags, baseIso, deltaIso, false, false, writer);

//        positions.forEach(p -> {
//            System.out.println("Insert pos: " + p.getNode().getKey() + " --- " + p.getIso());
//        });

        for(InsertPosition<K, G, V, T> pos : positions) {
            performAdd(key, pos, forceInsert, writer);
        }
    }

    public void printTree() {
        BiMap<V, V> baseIso = HashBiMap.create();
        printTree(rootNode, baseIso, IndentedWriter.stdout);
    }

    public void printTree(IndexNode<K, G, V, T> node, BiMap<V, V> transIso, IndentedWriter writer) {

        K prefKey = node.getKey();

        Set<K> altKeys = prefKey == null
                ? Collections.emptySet()
                : prefKeyToAltKeysWithIso.row(prefKey).keySet();
                //" keys: " + prefKey +
        writer.println("" + node.getKey() + " " + altKeys + " --- tags: " + node.getGraphTags() + " transIso: " + transIso);
        writer.incIndent();
        for(Edge<K, G, V, T> edge : node.getTargetKeyToEdges().values()) {
            K childKey = edge.getTo();
            IndexNode<K, G, V, T> childNode = keyToNode.get(childKey);
            BiMap<V, V> childTransIso = edge.getTransIso();

            printTree(childNode, childTransIso, writer);
        }
        writer.decIndent();
    }


    public static <T> Stream<T> reachableNodes(T node, Function<T, Stream<T>> nodeToChildren) {
        Stream<T> result = Stream.concat(
                Stream.of(node),
                nodeToChildren.apply(node).flatMap(v -> reachableNodes(v, nodeToChildren)));
        return result;
    }

    /**
     * Perform a lookup of children with tags, thereby adjusting the lookup set
     * while descending
     *
     * @param node
     * @param tags
     * @param adjuster
     * @param nodeToChildren
     * @return
     */
    public static <T, X> Stream<T> lookupChildrenByTags(T node, X tags, BiFunction<T, X, X> adjuster, BiFunction<T, X, Stream<T>> nodeToChildren) {
        Stream<T> result = nodeToChildren.apply(node, tags).flatMap(child -> {
            X adjustedTags = adjuster.apply(child, tags);
            Stream<T> subStream = lookupChildrenByTags(child, adjustedTags, adjuster, nodeToChildren);
            return subStream = Stream.concat(Stream.of(child), subStream);
        });
        return result;
    }




    public static <T, X> Stream<T> lookupProvidedChildrenByTags(Stream<T> children, X tags, BiFunction<T, X, X> adjuster, BiFunction<T, X, Stream<T>> nodeToChildren) {
        Stream<T> result = children.flatMap(child -> {
            X adjustedTags = adjuster.apply(child, tags);
            Stream<T> subStream = lookupChildrenByTags(child, adjustedTags, adjuster, nodeToChildren);
            return subStream = Stream.concat(Stream.of(child), subStream);
        });
        return result;
    }

    public static <T, X> Stream<T> reachableNodesWithParent(T node, X tags, BiFunction<T, X, X> adjuster, BiFunction<T, X, Stream<T>> nodeToChildren) {
        Stream<T> result = Stream.concat(
                Stream.of(node),
                nodeToChildren.apply(node, tags).flatMap(v -> {
                    X adjustedTags = adjuster.apply(node, tags);
                    return lookupChildrenByTags(v, adjustedTags, adjuster, nodeToChildren);
                }));
        return result;
    }

//    public static <T> Stream<T> breadthFirstStream(Collection<T> nodes, Function<T, Stream<T>> nodeToChildren) {
//        return breadthFirstStream(() -> nodes.stream(), nodeToChildren);
//    }

    public static <T> Stream<T> breadthFirstStream(T node, Function<T, Stream<T>> nodeToChildren) {
        Stream<T> result = Stream.concat(
                Stream.of(node),
                nodeToChildren.apply(node).flatMap(child -> breadthFirstStream(child, nodeToChildren)));

        return result;
    }

    /**
     *
     * @param nodes
     * @param nodeToChildren
     * @return
     */
    public static <T> Stream<T> breadthFirstSearchWithMultipleStartNodes(Collection<T> nodes, Function<T, Stream<T>> nodeToChildren) {
        Stream<T> result = Stream.concat(
                nodes.stream(),
                nodes.stream().flatMap(child -> breadthFirstStream(child, nodeToChildren)));

        return result;
    }



    public static <T, K, I extends Comparable<I>> Stream<T> breadthFirstSearchWithMultipleStartNodesAndOrderedChildren(Collection<T> nodes, Function<? super T, ? extends K> nodeToKey, Function<? super T, ? extends I> nodeToSize, Function<T, Stream<T>> nodeToChildren) {

        Set<K> seen = new HashSet<>(); //Sets.newIdentityHashSet();
        SetMultimap<I, T> sizeToNodes = Multimaps.newSetMultimap(new TreeMap<I, Collection<T>>(), () -> Sets.<T>newIdentityHashSet());

        for(T node : nodes) {
            K key = nodeToKey.apply(node);
            if(!seen.contains(key)) {
                seen.add(key);
                I size = nodeToSize.apply(node);
                sizeToNodes.put(size, node);
            }
        }

        Iterator<T> rIt = new AbstractIterator<T>() {
            @Override
            protected T computeNext() {
                T r;

                if(!sizeToNodes.isEmpty()) {
                    Iterator<Entry<I, T>> it = sizeToNodes.entries().iterator();
                    Entry<I, T> e = it.next();
                    it.remove();

                    r = e.getValue();

                    // Add the children of T to the open set
                    nodeToChildren.apply(r).forEach(child -> {
                        if(child != null) {
                            K key = nodeToKey.apply(child);
                            if(!seen.contains(key)) {
                                seen.add(key);
                                I size = nodeToSize.apply(child);
                                sizeToNodes.put(size, child);
                            }
                        }
                    });
                } else {
                    r = this.endOfData();
                }

                return r;
            }
        };


        Stream<T> result = Streams.stream(rIt);

        return result;
    }

    /**
     * There are two options to compute each key's graph in a subtree:
     * (1) We start with each key's complete graph, and then subtract the graph of the node
     * (2) Starting from the node, construct for an arbitrary node with that key the graph
     *
     * Approach 2:
     * For every key, pick a graph among those with a minimum depth (so we need the least number of
     * applyIso+union operations)
     * This means: perform breadth first, and
     *
     */
    public Map<K, G> loadGraphsInSubTrees(Map<K, G> result, G baseGraph, IndexNode<K, G, V, T> startNode, Collection<Edge<K, G, V, T>> edges) {
        for(Edge<K, G, V, T> edge : edges) {
            K targetNodeKey = edge.getTo();

            G targetGraph = result.computeIfAbsent(targetNodeKey, (tgn) -> {
                BiMap<V, V> transIso = edge.getTransIso();

                G residualGraph = edge.getResidualGraph();

                G transGraph = setOps.applyIso(baseGraph, transIso);
                G targetResidualGraph = setOps.union(transGraph, residualGraph);

                return targetResidualGraph;
            });

            // Recurse
            IndexNode<K, G, V, T> targetNode = keyToNode.get(targetNodeKey);
            Collection<Edge<K, G, V, T>> childEdges = targetNode.getTargetKeyToEdges().values();
            loadGraphsInSubTrees(result, targetGraph, targetNode, childEdges);
        }

        return result;
    }


    /**
     * Get the list of parents, reverse it and apply transIso and union
     *
     *
     *
     * @param node
     * @return
     */
    // Note: we could do function that assembles the graph for a specific key from a given start node
//    public G assembleGraphAtNode(GraphIndexNode<K, G, V, T> destNode, Predicate<GraphIndexNode<K, G, V, T>> isStartNode) {
//
//        // TODO Support memoization of assembledGraphs
//        G result = isStartNode.test(destNode)
//            ? destNode.getValue()
//            : setOps.union(
//                setOps.applyIso(assembleGraphAtNode(destNode.getParent(), isStartNode), destNode.getTransIso()),
//                destNode.getValue());
//
//        return result;
//    }

    public static <T> void parentsOf(T node, Function<T, T> getParent) {

    }


    protected K getPrefKey(K key) {
        Set<K> prefKeys = prefKeyToAltKeysWithIso.column(key).keySet();
        // Note: by the way the prefKeys set is constructed it must have only at most one entry
        K result = prefKeys.isEmpty()
                ? key
                : prefKeys.iterator().next();
        return result;
    }


    /**
     * There are 3 case for inserting a (k: K, g: Graph) pair at a certain node n:
     * (a) The residual graph g is empty, which means that the key is isomorphic to an existing one
     * (b) n does not have any children
     * (c) n does have children
     *
     *
     * These cases are treated as:
     * (a) The key k is added to the set of keys associated with n
     * (b) Get or create* a node for (k, residual graph) and attach it as a child to n
     * (c) Conceptually:
     *     For every (pref) key in n's sub tree, collect a map from that key to its residual graph (as seen from n)
     *     Order map entries by the size of the residual graphs, smallest first
     *     Then, remove all children from n, and re-insert the entries in order to n.
     *     The in-order insert guarantees, that case (c) cannot occur.
     *
     * * Get or create a node for (k, residual graph):
     *
     * k1: { ?a a Person }
     * k2: { ?b label ?c }
     * k3: { ?s a Person ; label ?o }
     * k4: { ?x a Person ; label ?y ; age ?z }
     * k5: { ?o age ?p }
     * k6: { ?h a Person ; label ?i . ?a ?b ?c . ?x ?y ?z }
     *
     *
     * Another possible issue: Disconnected sub-sets. Any iso we find for ?a ?b ?c
     * does not 'constrain' the other patterns. So it may clutter up the index because it may well become
     * a leaf of many nodes in the tree. Not sure if we can do better by a higher level index that
     * splits up the sets and indexes them individually.
     *
     *
     *   / k1: { ?a a Person } - k3: { ?s label ?o } \
     * {}                                              ??? k3 -> k4 ???
     *   \ k2: { ?b label ?c } - k3: { ?s a Person } /
     *
     * I am now unsure about whether we can optimize something due to diamond patterns:
     *
     * k3: { ?s label ?o } means:
     * If some query residual graph q is isomorphic to { ?s label ?o } it is a match for k3
     *
     * But then, whenever for a new graph e.g. k4 it turns out there is an iso from another
     * graph k3, won't k4 then become a child to every node corresponding to k3?
     * But then, we can just record: k3 -> k4.
     * Yet, k4 would also be a child of k5
     *
     *
     * So the change would be, that in the index, keys become nodes,
     * and the residual graph becomes part of the edge
     * Graph<K, Edge>
     * Edge: {
     *   // "By going from fromKey and mapping via transIso we reach toKey; with residualGraph remaining
     *   K fromKey;
     *   BiMap<N, N> transIso;
     *   K toKey;
     *   Graph residualGraph;
     * }
     *
     *
     * Note: residual graphs could be isomorphic to each other
     *
     *
     * @param key
     * @param pos
     * @param forceInsert
     * @param writer
     */
    void performAdd(K key, InsertPosition<K, G, V, T> pos, boolean forceInsert, IndentedWriter writer) {
        IndexNode<K, G, V, T> nodeA = pos.getNode();
        //Graph insertGraphIsoB = pos.getGraphIso();

        G residualInsertGraphB = pos.getResidualQueryGraph();
        Set<T> residualInsertGraphBTags = pos.getResidualQueryGraphTags();


        // If the insert graph is empty, just append the key to the insert node
        // i.e. do not create a child node
        // In this case,
        if(isEmpty(residualInsertGraphB)) {
            K prefKey = nodeA.getKey();
            Map<K, BiMap<V, V>> altKeyToIso = prefKeyToAltKeysWithIso.row(prefKey);

            BiMap<V, V> deltaIso = removeIdentity(pos.getIso());
            altKeyToIso.put(key, deltaIso);

            return;
        }

        BiMap<V, V> isoAB = pos.getLatestIsoAB();
        BiMap<V, V> baseIsoAB = pos.getIso();

        // TODO isoAB should have an empty intersection with iso parentOfAtoA
        // Make a sanity check to ensure that

        // If nodeA does not have children, we just append nodeB
        // Otherwise, we need to do a more complex update procedure where we
        // re-insert every graph in the node's subtree
        IndexNode<K, G, V, T> nodeB = createNode(key, null, null); //residualInsertGraphB, residualInsertGraphBTags);
        if(nodeA.isLeaf() || forceInsert) { //nodeA.childIndex.isEmpty()) {

            nodeA.appendChild(nodeB, residualInsertGraphB, residualInsertGraphBTags, isoAB);
            K prefKey = getPrefKey(key);
            Map<K, BiMap<V, V>> altKeyToIso = prefKeyToAltKeysWithIso.row(prefKey);
            altKeyToIso.put(key, HashBiMap.create());

        } else {

            Set<Edge<K, G, V, T>> directCandEdges =
                    nodeA.getEdgeIndex().getAllSupersetsOf(residualInsertGraphBTags, false).keySet();

            List<Edge<K, G, V, T>> directNonCandEdges =
                    nodeA.getTargetKeyToEdges().values().stream()
                    .filter(edge -> !directCandEdges.contains(edge))
                    .collect(Collectors.toList());

            // Find all residual graphs reachable from this node and order them by size

            Map<K, G> keyToGraph = loadGraphsInSubTrees(new HashMap<>(), setOps.createNew(), nodeA, directCandEdges);
            //System.out.println("Found these graphs in subtree of candidate children: " + keyToGraph.keySet());

            List<Entry<K, G>> keyGraphs = new ArrayList<>(keyToGraph.entrySet());
            Collections.sort(keyGraphs, (a, b) -> setOps.size(a.getValue()) - setOps.size(b.getValue()));


            // Remove all edges reachable via directCandEdges - i.e. clear data of all reachable nodes
            Set<IndexNode<K, G, V, T>> reachableNodes = directCandEdges.stream()
                .map(Edge::getTo)
                .map(keyToNode::get)
                .flatMap(node -> reachableNodes(node,
                        n -> n.getTargetKeyToEdges().keySet().stream().map(keyToNode::get)))
                .collect(Collectors.toCollection(Sets::newIdentityHashSet));

            reachableNodes.forEach(node -> {
                node.clearLinks(true);
            });

            // Create the node that will replace nodeA, and
            // create and append the insert node as a child
            //TagMap<Edge<K, G, V, T>, T> tagMap = new TagMapSetTrie<>(tagComparator);
            //IndexNode<K, G, V, T> replacementNodeA = new IndexNode<>(nodeA.getKey(), nodeA.getGraph(), nodeA.getGraphTags(), tagMap);

            // Clear nodeA and append the insert data as the only child
            nodeA.clearLinks(false);
            nodeA.appendChild(nodeB, residualInsertGraphB, residualInsertGraphBTags, isoAB);


            // Perform additions
            for(Entry<K, G> keyGraph : keyGraphs) {
                K k = keyGraph.getKey();
                G insertGraph = keyGraph.getValue();
                Set<T> insertGraphTags = extractGraphTagsWrapper(insertGraph);
                //BiMap<V, V> baseIso = HashBiMap.create();

                add(nodeA, k, insertGraph, insertGraphTags, baseIsoAB, true, writer);
            }

            // Append all unaffected non-candidate children
            for(Edge<K, G, V, T> edge : directNonCandEdges) {
                K targetNodeKey = edge.getTo();
                IndexNode<K, G, V, T> targetNode = keyToNode.get(targetNodeKey);
                nodeA.appendChild(targetNode, edge.getResidualGraph(), edge.getResidualGraphTags(), edge.getTransIso());
            }
        }
    }


}


// Lots of useless code below; remove at some point


/**
 * This method is based on a conceptual error.
 * When inserting new graph at a node, we need to replay the
 * insert of all children; rather then just checking whether the newly
 * inserted graph is a sub-graph iso of a direct child.
 *
 * @param key
 * @param pos
 * @param writer
 */
//void performAddBullshit(K key, InsertPosition<K, G, V, T> pos, IndentedWriter writer) {
//    GraphIndexNode<K, G, V, T> nodeA = pos.getNode();
//    //Graph insertGraphIsoB = pos.getGraphIso();
//
//    G residualInsertGraphB = pos.getResidualQueryGraph();
//    Set<T> residualInsertGraphBTags = pos.getResidualQueryGraphTags();
//
//
//    // If the insert graph is empty, just append the key to the insert node
//    // i.e. do not create a child node
//    if(isEmpty(residualInsertGraphB)) {
//        nodeA.getKeys().add(key);
//        return;
//    }
//
//
//
//    BiMap<V, V> isoAB = pos.getLatestIsoAB();
//    BiMap<V, V> baseIsoAB = pos.getIso();
//
//    // TODO isoAB should have an empty intersection with iso parentOfAtoA
//    // Make a sanity check to ensure that
//
//    GraphIndexNode<K, G, V, T> nodeB = createNode(residualInsertGraphB, residualInsertGraphBTags, isoAB);
//    nodeB.getKeys().add(key);
//
//    writer.println("Insert attempt of user graph of size " + setOps.size(residualInsertGraphB));
////    RDFDataMgr.write(System.out, insertGraph, RDFFormat.NTRIPLES);
////    System.out.println("under: " + currentIso);
//
//    // If the addition is not on a leaf node, check if we subsume anything
//    boolean isSubsumed = nodeA.getChildren().stream().filter(c -> !c.getKeys().contains(key)).count() == 0;//;isEmpty(); //{false};
//
//
//    // TODO We must not insert to nodes where we just inserted
//
//    // Make a copy of the baseIso, as it is transient due to state space search
//    //GraphIsoMap gim = new GraphIsoMapImpl(insertGraph, HashBiMap.create(baseIso));
//
//    //boolean wasAdded = false;
//
//    // If the insertGraph was not subsumed,
//    // check if it subsumes any of the other children
//    // for example { ?s ?p ?o } may not be subsumed by an existing child, but it will subsume any other children
//    // use clusters
//    // add it as a new child
//    if(!isSubsumed) {
//        writer.println("We are not subsumed, but maybe we subsume");
////        GraphIndexNode<K> nodeB = null;//createNode(graphIso);//new GraphIndexNode<K>(graphIso);
//
//
//        writer.incIndent();
//        //for(GraphIndexNode child : children) {
//        //Iterator<GraphIndexNode<K>> it = nodeA.getChildren().iterator();//children.listIterator();
//        Iterator<GraphIndexNode<K, G, V, T>> it = new ArrayList<>(nodeA.getChildren()).iterator();
//        while(it.hasNext()) {
//            GraphIndexNode<K, G, V, T> nodeC = it.next();
//            G viewGraphC = nodeC.getValue();
//            Set<T> viewGraphCTags = nodeC.getGraphTags();
//
//            writer.println("Comparison with view graph of size " + setOps.size(viewGraphC));
////            RDFDataMgr.write(System.out, viewGraph, RDFFormat.NTRIPLES);
////            System.out.println("under: " + currentIso);
//
//            // For every found isomorphism, check all children whether they are also isomorphic.
//            writer.incIndent();
//            int i = 0;
//
//            boolean isSubsumedC = false;
////baseIso: ?x -> ?y, transIso: ?x -> ?z => ?y -> ?z
//            BiMap<V, V> transIsoAC = nodeC.getTransIso();
//            BiMap<V, V> transBaseIsoBC = chain(baseIsoAB.inverse(), transIsoAC);
//            //mapDomainVia(nodeC.getTransIso(), );
//
//
//            //Iterable<BiMap<V, V>> isosBC = isoMatcher.match(baseIso.inverse(), residualInsertGraphB, viewGraphC);//QueryToJenaGraph.match(baseIso.inverse(), residualInsertGraphB, viewGraphC).collect(Collectors.toSet());
//            Iterable<BiMap<V, V>> isosBC = isoMatcher.match(transBaseIsoBC, residualInsertGraphB, viewGraphC);//QueryToJenaGraph.match(baseIso.inverse(), residualInsertGraphB, viewGraphC).collect(Collectors.toSet());
////            isosBC = Lists.newArrayList(isosBC);
////            System.out.println("Worked A!");
//            for(BiMap<V, V> isoBC : isosBC) {
//                isSubsumedC = true;
//                writer.println("Detected subsumption #" + ++i + " with iso: " + isoBC);
//                writer.incIndent();
//
//                // We found an is from B to C, where there was a prior iso from A to C
//                // This means, we need to update the transIso of C as if we were coming from B instead of A
//
//                // TODO FUCK! This isoGraph object may be a reason to keep the original graph and the iso in a combined graph object
//                //nodeB = nodeB == null ? createNode(residualInsertGraphB, isoAB) : nodeB;
//                G mappedResidualInsertGraphC = setOps.applyIso(residualInsertGraphB, isoBC);
//                Set<T> mappedResidualInsertGraphCTags = residualInsertGraphBTags;
//                G removalGraphC = setOps.intersect(mappedResidualInsertGraphC, viewGraphC);
//
//                Set<T> removalGraphCTags = Sets.intersection(mappedResidualInsertGraphCTags, viewGraphCTags);
//
//
//                BiMap<V, V> transIsoBAC = chain(baseIsoAB.inverse(), transIsoAC);
//
//                // The isoBC must be a subset of transIsoAC (because A subgraphOf B subgraphOf C)
//                // Get the mappings that are in common, so we can subtract them
//                BiMap<V, V> removalIsoBC = HashBiMap.create(Maps.difference(isoBC, transIsoBAC).entriesInCommon());
//
//
//                 // BiMap<V, V> deltaIsoABC = HashBiMap.create(Maps.difference(isoBC, transIsoAC).entriesInCommon());
//                //System.out.println("deltaIsoBC: " + deltaIsoBC);
//
//                //BiMap<V, V> removalTransIsoBC = HashBiMap.create(Maps.difference(isoBC, transIsoAC).entriesInCommon());
//
//
////                BiMap<V, V> newTransIsoC = mapDomainVia(nodeC.getTransIso(), isoBC);
//                //BiMap<V, V> newTransIsoC = mapDomainVia(nodeC.getTransIso(), isoBC);
//                //System.out.println("NewTransIsoC: " + newTransIsoC);
//
//
//
//                GraphIndexNode<K, G, V, T> newChildC = cloneWithRemoval(nodeC, isoBC, removalIsoBC, removalGraphC, removalGraphCTags, writer);
//                nodeB.appendChild(newChildC);//add(newChild, baseIso, writer);
//
//
//                writer.decIndent();
//            }
//
//            if(isSubsumedC) {
//                deleteNode(nodeC.getKey());
//            }
//
//
////            if(nodeB != null) {
////                //it.remove();
////
////                //nodeB.getKeys().add(key);
////
////                writer.println("A node was subsumed and therefore removed");
////                //wasAdded = true;
////                // not sure if this remove works
////            }
//            writer.decIndent();
//
//        }
//        writer.decIndent();
//
//    }
//
//    // If nothing was subsumed, add it to this node
//    //if(!wasAdded) {
//        writer.println("Attached graph of size " + setOps.size(residualInsertGraphB) + " to node " + nodeA);
//        nodeA.appendChild(nodeB);
//        //GraphIndexNode<K> target = createNode(residualInsertGraphB, baseIso);
//        //target.getKeys().add(key);
//        //nodeA.appendChild(target);
//    //}
//}


//
//Iterable<BiMap<Node, Node>> isoTmp = Lists.newArrayList(toIterable(QueryToJenaGraph.match(baseIso.inverse(), insertGraph, viewGraph)));
//
//GraphVar ga = new GraphVarImpl();
////insertGraph.find(Node.ANY, Node.ANY, Node.ANY).forEachRemaining(ga::add);
//GraphUtil.addInto(ga, insertGraph);
//ga.find(Node.ANY, Node.ANY, Var.alloc("ao")).forEachRemaining(x -> System.out.println(x));
//GraphVar gb = new GraphVarImpl();
//viewGraph.find(Node.ANY, Node.ANY, Node.ANY).forEachRemaining(gb::add);
////GraphUtil.addInto(gb, viewGraph);
//insertGraph = ga;
//viewGraph = new GraphIsoMapImpl(gb, HashBiMap.create());

//                    System.out.println("Remapped insert via " + iso);
//RDFDataMgr.write(System.out, insertGraphX, RDFFormat.NTRIPLES);
//System.out.println("---");

//Difference retain = new Difference(viewGraph, insertGraphX);

// The part which is duplicated between the insert graph and the view
// is subject to removal
//Intersection removalGraph = new Intersection(mappedInsertGraph, viewGraphC);

// Allocate root before child to give it a lower id for cosmetics
//nodeB = nodeB == null ? createNode(mappedInsertGraph) : nodeB;



//protected ProblemNeighborhoodAware<BiMap<V, V>, V> toProblem(InsertPosition<?, G, V> pos) {
//  BiMap<V, V> baseIso = pos.getIso();
//  G residualQueryGraph = pos.getResidualQueryGraph();
//
//  // TODO This looks wrong, why an empty graph here?!
//  G residualViewGraph = setOps.createNew(); // new
//                                              // GraphVarImpl();//pos.getNode().getValue();
//                                              // //new
//                                              // GraphIsoMapImpl(pos.getNode().getValue(),
//                                              // pos.getNode().getTransIso());
//                                              // //pos.getNode().getValue();
//
//  // QueryToJenaGraph::createNodeComparator,
//  // QueryToJenaGraph::createEdgeComparator);
//  ProblemNeighborhoodAware<BiMap<V, V>, V> result = isoMatcher.match(baseIso, residualViewGraph, residualQueryGraph);
//
//  return result;
//}

//protected Iterable<BiMap<V, V>> toProblem(InsertPosition<?, G, V, ?> pos) {
//  BiMap<V, V> baseIso = pos.getIso();
//  G residualQueryGraph = pos.getResidualQueryGraph();
//
//  // TODO This looks wrong, why an empty graph here?!
//  G residualViewGraph = setOps.createNew(); // new
//                                              // GraphVarImpl();//pos.getNode().getValue();
//                                              // //new
//                                              // GraphIsoMapImpl(pos.getNode().getValue(),
//                                              // pos.getNode().getTransIso());
//                                              // //pos.getNode().getValue();
//
//  // QueryToJenaGraph::createNodeComparator,
//  // QueryToJenaGraph::createEdgeComparator);
//  Iterable<BiMap<V, V>> result = isoMatcher.match(baseIso, residualViewGraph, residualQueryGraph);
//
//  return result;
//}
//
//protected Iterable<BiMap<V, V>> createProblem(Collection<? extends InsertPosition<?, G, V, ?>> poss) {
//  Iterable<BiMap<V, V>> result = () -> poss.stream()
//          //The following two lines are equivalent to .flatMap(pos -> Streams.stream(toProblem(pos)))
//          .map(this::toProblem)
//          .flatMap(Streams::stream)
//          .distinct()
//          .iterator();
//          //.collect(Collectors.toList());
//
//  return result;
//}
//


//
//public Map<K, ProblemNeighborhoodAware<BiMap<V, V>, V>> lookupStream2(G queryGraph, boolean exactMatch) {
//  Multimap<K, InsertPosition<K, G, V, T>> matches = lookup(queryGraph, exactMatch);
//
//  Map<K, ProblemNeighborhoodAware<BiMap<V, V>, V>> result =
//      matches.asMap().entrySet().stream()
//          .collect(Collectors.toMap(
//                  Entry::getKey,
//                  e -> createProblem(e.getValue())
//                  ));
//
//  return result;
////              Map<K, ProblemNeighborhoodAware<BiMap<Var, Var>, Var>> result = matches.asMap().entrySet().stream()
////                      .collect(Collectors.toMap(e -> e.getKey(), e -> SparqlViewMatcherQfpcIso.createCompound(e.getValue())));
////
////
////  BiMap<Node, Node> baseIso = pos.getIso();
////
////
////  System.out.println("RAW SOLUTIONS for " + pos.getNode().getKey());
////  rawProblem.generateSolutions().forEach(s -> {
////      System.out.println("  Raw Solution: " + s);
////  });
////
////  ProblemNeighborhoodAware<BiMap<Var, Var>, Var> result = new ProblemVarWrapper(rawProblem);
////
////
////  return result;
//}



//Map<Long, Iterable<BiMap<V, V>>> tmp =
//  matches.asMap().entrySet().stream()
//      .collect(Collectors.toMap(
//              Entry::getKey,
//              e -> createProblem(e.getValue())));


//Map<K, Iterable<BiMap<V, V>>> result = tmp.entrySet().stream()
//  .flatMap(e -> idToKeys.get(e.getKey()).stream().map(key -> new SimpleEntry<>(key, e.getValue())))
//  .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

// TODO Include all alternative keys
//Map<K, Iterable<BiMap<V, V>>> result = tmp.entrySet().stream()
//.flatMap(e -> idToPrefKey.get(e.getKey()).stream().map(key -> new SimpleEntry<>(key, e.getValue())))
//.map(e -> new SimpleEntry<>(idToPrefKey.get(e.getKey()), e.getValue()))
//.collect(Collectors.toMap(Entry::getKey, Entry::getValue));




//
///**
//* Clones a sub tree thereby removing the triples in the removal graph
//* TODO: How to update the remaining isomorphisms?
//*
//*
//*
//*
//* @param removalGraphC
//* @param writer
//* @return
//*/
//// Note: isoBC will be equivalent to nodeC.getTransIso() on recursion, but the first call will override it
////       - so this set of tags depends on the parent node
//GraphIndexNode<K, G, V, T> cloneWithRemoval(GraphIndexNode<K, G, V, T> nodeC, BiMap<V, V> baseIso, BiMap<V, V> removalTransIsoBC, G removalGraphC, Set<T> removalGraphCTags, IndentedWriter writer) { //BiMap<Node, Node> isoBC, Graph residualInsertGraphB,
//  G graphC = nodeC.getValue();
//
//  G residualGraphC = setOps.difference(graphC, removalGraphC);
//  Set<T> residualGraphCTags = Sets.difference(nodeC.getGraphTags(), removalGraphCTags);
//
//  if(logger.isDebugEnabled()) {
//      logger.debug("At node " + nodeC.getId() + ": Cloned graph size reduced from  " + setOps.size(graphC) + " -> " + setOps.size(residualGraphC));
//  }
//
//  BiMap<V, V> isoNewBC = HashBiMap.create(
//          Maps.difference(nodeC.getTransIso(), removalTransIsoBC).entriesOnlyOnLeft());
//
//  //BiMap<V, V> isoNewBC = HashBiMap.create(nodeC.getTransIso());
//  //isoNewBC.entrySet().removeAll(isoBC.entrySet());
////          BiMap<V, V> deltaIso = HashBiMap.create(
////                  Maps.inte(iso, transBaseIso).entriesOnlyOnLeft());
//
//  GraphIndexNode<K, G, V, T> newNodeC = createNode(residualGraphC, residualGraphCTags, isoNewBC);
//
//
////  GraphIndexNode<K, G, V, T> newNodeC = createNode(residualGraphC, residualGraphCTags, isoBC);
//  //newNodeC.getKeys().addAll(nodeC.getKeys());
//
//
//  // Then for each child: map the removal graph according to the child's iso
//  for(GraphIndexNode<K, G, V, T> nodeD : nodeC.getChildren()) {
//
//      BiMap<V, V> isoCD = nodeD.getTransIso();
//
//      BiMap<V, V> newBaseIso = mapDomainVia(baseIso, isoCD);
//
////      GraphIsoMap removalGraphD = new GraphIsoMapImpl(removalGraphC, isoCD);
//      G removalGraphD = setOps.applyIso(removalGraphC, newBaseIso); //isoCD);
//
//      BiMap<V, V> removalTransIsoCD = mapDomainVia(removalTransIsoBC, isoCD);
//
//      // NOTE Graph tags are unaffected by isomorphism
//      Set<T> removalGraphDTags = removalGraphCTags;
//
//      GraphIndexNode<K, G, V, T> cloneChild = cloneWithRemoval(nodeD, newBaseIso, removalTransIsoCD, removalGraphD, removalGraphDTags, writer);
//      //deleteNode(child.getKey());
//      newNodeC.appendChild(cloneChild);
//  }
//
//  long[] nodeIds = nodeC.getChildren().stream().mapToLong(GraphIndexNode::getId).toArray();
//  for(Long nodeId : nodeIds) {
//      deleteNode(nodeId);
//  }
//
//
//
//  return newNodeC;
//}



// Iterate all children whose graphTags are a super set of the given one
// Thereby recursively updating the lookup tag set with the graphTags covered by a node
//Stream<GraphIndexNode<K, G, V, T>> candChildren = lookupProvidedChildrenByTags(
//        directCandChildren.stream(),
//        residualInsertGraphBTags,
//        (node, tags) -> Sets.difference(tags, node.getGraphTags()),
//        (node, tags) -> node.childIndex.getAllSupersetsOf(tags, false)
//                .keySet().stream().map(nodeId -> nodeA.idToChild.get(nodeId))
//        );


//Collection<GraphIndexNode<K, G, V, T>> tmp = candChildren.collect(Collectors.toList());
//
//candChildren = tmp.stream();
//
//// For every key in the sub-tree, collect one representative
//// residual graph.
//Map<K, G> keyToGraph = new HashMap<>();
//Multimap<K, K> altKeys = HashMultimap.create();
//candChildren.forEach(node -> {
//    // Pick one key as the representative key and use the others as alt keys
//    Collection<K> keys = new HashSet<>(idToKeys.get(node.getId()));
//    idToKeys.removeAll(node.getId());
//
//    if(!keys.isEmpty()) {
//        K repKey = keys.iterator().next();
//        for(K altKey : keys) {
//            if(!Objects.equals(repKey, altKey)) {
//                altKeys.put(repKey, altKey);
//            }
//        }
//
//        // For the repKey, build the graph
//        // The baseGraph is this node's parent keyGraph
//        Collection<K> parentKeys = keys;///idToKeys.get(node.getId());
//        K parentRepKey = Iterables.getFirst(parentKeys, null);
//        G baseGraph = keyToGraph.get(parentRepKey);
//        G keyGraph = baseGraph == null
//                ? node.getValue()
//                : setOps.union(setOps.applyIso(baseGraph, node.transIso), node.graph)
//                ;
//
//        keyToGraph.put(repKey, keyGraph);
//    }
//});
