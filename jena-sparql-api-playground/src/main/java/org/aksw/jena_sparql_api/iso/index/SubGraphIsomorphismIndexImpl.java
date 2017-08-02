package org.aksw.jena_sparql_api.iso.index;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collections.MapUtils;
import org.aksw.commons.collections.multimaps.BiHashMultimap;
import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.aksw.commons.collections.set_trie.TagMap;
import org.aksw.commons.collections.set_trie.TagMapSetTrie;
import org.aksw.commons.collections.trees.ReclaimingSupplier;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.ext.com.google.common.base.Predicate;
import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;

/**
 * Generic sub graph isomorphism index class that is essentially a Map.
 * Keys of type K are associated with a graph/set of type G having items of type V.
 *
 * Instances are parameterized with set operations, an isomorphims matcher and an optional
 * graph tagger.
 *
 * @author raven
 *
 * @param <K>
 */
public class SubGraphIsomorphismIndexImpl<K, G, V, T> implements SubGraphIsomorphismIndex<K, G, V>
//    extends LabeledTree<Long, //GraphIndexNode<K>>
{
    private static final Logger logger = LoggerFactory.getLogger(SubGraphIsomorphismIndexImpl.class);

    protected IsoMatcher<G, V> isoMatcher;
    // Optional function that extracts a set of static features from a graph
    // Typically this is the set of static node and/or edge labels
    // The feature set must be consistent with the isomorphism relation:
    // If there is a subgraph iso from '(n)eedle' to '(h)aystack, then
    // the features of n must be a subset of that of h
    // existsIso(n, h) implies features(n) subsetOf features(h)
    //protected Function<G, Collection<?>> graphToStaticFeatures;
    //protected abstract Collection<?> extractGraphTags(G graph);
    protected Function<? super G, Collection<T>> extractGraphTags;
    protected Comparator<? super T> tagComparator;
    protected SetOps<G, V> setOps;

    protected Set<T> extractGraphTagsWrapper(G graph) {
        Collection<T> tmp = extractGraphTags.apply(graph);
        Set<T> result = tmp.stream().collect(Collectors.toSet());
        return result;
    }


    protected long root;
    protected GraphIndexNode<K, G, V, T> rootNode;
    protected Supplier<Long> idSupplier;
    protected Map<Long, GraphIndexNode<K, G, V, T>> keyToNode = new HashMap<>();

    protected Supplier<K> keySupplier;
    protected IBiSetMultimap<Long, K> idToKeys = new BiHashMultimap<>();

    // If a key k2's graph is isomorphic to that of a key k1, then k2 becomes an alt key of k1
    //protected ReversibleMap<K, Long> keysToNodeId = new ReversibleMapImpl<>();

    public SubGraphIsomorphismIndexImpl(
            Supplier<K> keySupplier,
            SetOps<G, V> setOps,
            Function<? super G, Collection<T>> extractGraphTags,
            Comparator<? super T> tagComparator,
            IsoMatcher<G, V> isoMatcher
            ) {
        super();
        this.keySupplier = keySupplier;
        this.setOps = setOps;
        this.extractGraphTags = extractGraphTags;
        this.tagComparator = tagComparator;
        this.isoMatcher = isoMatcher;

        long i[] = {0};
        idSupplier = new ReclaimingSupplier<>(() -> i[0]++);


        rootNode = createNode(setOps.createNew(), Collections.emptySet(), HashBiMap.create());
        //rootNode = createNode(new GraphIsoMapImpl(new GraphVarImpl(), HashBiMap.create()));
        root = rootNode.getId();

    }


    //Map<Long, GraphIsoMap> idToGraph = new HashMap<>();


//    long nextId[] = {0};
//    protected ReclaimingSupplier<Long> idSupplier = new ReclaimingSupplier<>(() -> nextId[0]++ );


//    protected GraphIndexNode<K> root = new GraphIndexNode<K>(new GraphIsoMapImpl(new GraphVarImpl(), HashBiMap.create()));
//
//    protected Map<Long, GraphIndexNode<K>> IdToNode = new HashMap<>();
//    protected ReversibleMap<Long, Long> childToParent = new ReversibleMapImpl<>();




    /* (non-Javadoc)
     * @see org.aksw.jena_sparql_api.iso.index.SubGraphIsomorphismIndex#removeKey(java.lang.Object)
     */
    @Override
    public void removeKey(Object key) {
        // Copy id set to avoid concurrent modification
        //Set<Long> ids = idToKeys.getInverse().get(key);
        //Set<Long> ids = new HashSet<>(idToKeys.getInverse().get(key));
        idToKeys.getInverse().removeAll(key);
//        for(Long id : ids) {
//            keyToNode.get(id).getKeys().remove(key);
//            //deleteNode(id);
//        }
    }

//    public void removeNodeById(Long id) {
//    	delet
//        //throw new UnsupportedOperationException("not implemented");
//    }


    /**
     * Create a detached node
     *
     * @param graphIso
     * @return
     */
    protected GraphIndexNode<K, G, V, T> createNode(G graph, Set<T> graphTags, BiMap<V, V> transIso) {
        Long id = idSupplier.get();

//        Set<Object> graphTags = extractGraphTagsWrapper(graph);

        TagMap<Long, T> tagMap = new TagMapSetTrie<>(tagComparator);
        GraphIndexNode<K, G, V, T> result = new GraphIndexNode<>(null, id, transIso, graph, graphTags, tagMap);
        keyToNode.put(id, result);


        logger.debug("Created node with id " + id + " and transIso " + result.transIso);
        return result;
    }



    /**
     * Insert the graph and allocate a fresh, unused, id
     *
     * @param graph
     */
    public K add(G graph) {
        K key = keySupplier.get();

        put(key, graph);

        return key;
    }


    public Map<K, Iterable<BiMap<V, V>>> lookupStream(G queryGraph, boolean exactMatch) {
        Multimap<Long, InsertPosition<K, G, V, T>> matches = lookup(queryGraph, exactMatch);

        Map<Long, Iterable<BiMap<V, V>>> tmp =
            matches.asMap().entrySet().stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                        e -> createProblem(e.getValue())));


        Map<K, Iterable<BiMap<V, V>>> result = tmp.entrySet().stream()
            .flatMap(e -> idToKeys.get(e.getKey()).stream().map(key -> new SimpleEntry<>(key, e.getValue())))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));


        return result;
    }

//    protected ProblemNeighborhoodAware<BiMap<V, V>, V> toProblem(InsertPosition<?, G, V> pos) {
//        BiMap<V, V> baseIso = pos.getIso();
//        G residualQueryGraph = pos.getResidualQueryGraph();
//
//        // TODO This looks wrong, why an empty graph here?!
//        G residualViewGraph = setOps.createNew(); // new
//                                                    // GraphVarImpl();//pos.getNode().getValue();
//                                                    // //new
//                                                    // GraphIsoMapImpl(pos.getNode().getValue(),
//                                                    // pos.getNode().getTransIso());
//                                                    // //pos.getNode().getValue();
//
//        // QueryToJenaGraph::createNodeComparator,
//        // QueryToJenaGraph::createEdgeComparator);
//        ProblemNeighborhoodAware<BiMap<V, V>, V> result = isoMatcher.match(baseIso, residualViewGraph, residualQueryGraph);
//
//        return result;
//    }

    protected Iterable<BiMap<V, V>> toProblem(InsertPosition<?, G, V, ?> pos) {
        BiMap<V, V> baseIso = pos.getIso();
        G residualQueryGraph = pos.getResidualQueryGraph();

        // TODO This looks wrong, why an empty graph here?!
        G residualViewGraph = setOps.createNew(); // new
                                                    // GraphVarImpl();//pos.getNode().getValue();
                                                    // //new
                                                    // GraphIsoMapImpl(pos.getNode().getValue(),
                                                    // pos.getNode().getTransIso());
                                                    // //pos.getNode().getValue();

        // QueryToJenaGraph::createNodeComparator,
        // QueryToJenaGraph::createEdgeComparator);
        Iterable<BiMap<V, V>> result = isoMatcher.match(baseIso, residualViewGraph, residualQueryGraph);

        return result;
    }

    protected Iterable<BiMap<V, V>> createProblem(Collection<? extends InsertPosition<?, G, V, ?>> poss) {
        Iterable<BiMap<V, V>> result = () -> poss.stream()
                //The following two lines are equivalent to .flatMap(pos -> Streams.stream(toProblem(pos)))
                .map(this::toProblem)
                .flatMap(Streams::stream)
                .distinct()
                .iterator();
                //.collect(Collectors.toList());

        return result;
    }
//
//    public Map<K, ProblemNeighborhoodAware<BiMap<V, V>, V>> lookupStream2(G queryGraph, boolean exactMatch) {
//        Multimap<K, InsertPosition<K, G, V, T>> matches = lookup(queryGraph, exactMatch);
//
//        Map<K, ProblemNeighborhoodAware<BiMap<V, V>, V>> result =
//            matches.asMap().entrySet().stream()
//                .collect(Collectors.toMap(
//                        Entry::getKey,
//                        e -> createProblem(e.getValue())
//                        ));
//
//        return result;
////                    Map<K, ProblemNeighborhoodAware<BiMap<Var, Var>, Var>> result = matches.asMap().entrySet().stream()
////                            .collect(Collectors.toMap(e -> e.getKey(), e -> SparqlViewMatcherQfpcIso.createCompound(e.getValue())));
////
////
////        BiMap<Node, Node> baseIso = pos.getIso();
////
////
////        System.out.println("RAW SOLUTIONS for " + pos.getNode().getKey());
////        rawProblem.generateSolutions().forEach(s -> {
////            System.out.println("  Raw Solution: " + s);
////        });
////
////        ProblemNeighborhoodAware<BiMap<Var, Var>, Var> result = new ProblemVarWrapper(rawProblem);
////
////
////        return result;
//    }

    /* (non-Javadoc)
     * @see org.aksw.jena_sparql_api.iso.index.SubGraphIsomorphismIndex#lookup(G, boolean)
     */
    //@Override
    public Multimap<Long, InsertPosition<K, G, V, T>> lookup(G queryGraph, boolean exactMatch) {

        Set<T> queryGraphTags = extractGraphTagsWrapper(queryGraph);

        Collection<InsertPosition<K, G, V, T>> positions = new LinkedList<>();
        findInsertPositions(positions, rootNode, queryGraph, queryGraphTags, HashBiMap.create(), HashBiMap.create(), true, exactMatch, IndentedWriter.stderr);

        Multimap<Long, InsertPosition<K, G, V, T>> result = HashMultimap.create();
        logger.debug("Lookup result candidates: " + positions.size());
        for(InsertPosition<K, G, V, T> pos : positions) {
            // Match with the children

            result.put(pos.getNode().getId(), pos);
            //System.out.println("Node " + pos.node + " with keys " + pos.node.getKeys() + " iso: " + pos.getGraphIso().getInToOut());
//            for(K key : pos.node.getKeys()) {
//                result.put(key, pos);
//            }
        }
        return result;
    }





    public Multimap<Long, BiMap<V, V>> lookupFlat(G queryGraph, boolean exactMatch) {

        Set<T> queryGraphTags = extractGraphTagsWrapper(queryGraph);


        Collection<InsertPosition<K, G, V, T>> positions = new LinkedList<>();
        findInsertPositions(positions, rootNode, queryGraph, queryGraphTags, HashBiMap.create(), HashBiMap.create(), true, exactMatch, IndentedWriter.stderr);

        Multimap<Long, BiMap<V, V>> result = HashMultimap.create();
        logger.debug("Lookup result candidates: " + positions.size());
        for(InsertPosition<K, G, V, T> pos : positions) {
            // Match with the children

            result.put(pos.getNode().getId(), pos.getIso());
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

        add(rootNode, key, graph, insertGraphTags, HashBiMap.create(), false, IndentedWriter.stderr);

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


    /**
     * Clones a sub tree thereby removing the triples in the removal graph
     * TODO: How to update the remaining isomorphisms?
     *
     *
     *
     *
     * @param removalGraphC
     * @param writer
     * @return
     */
    // Note: isoBC will be equivalent to nodeC.getTransIso() on recursion, but the first call will override it
    //       - so this set of tags depends on the parent node
    GraphIndexNode<K, G, V, T> cloneWithRemoval(GraphIndexNode<K, G, V, T> nodeC, BiMap<V, V> baseIso, BiMap<V, V> removalTransIsoBC, G removalGraphC, Set<T> removalGraphCTags, IndentedWriter writer) { //BiMap<Node, Node> isoBC, Graph residualInsertGraphB,
        G graphC = nodeC.getValue();

        G residualGraphC = setOps.difference(graphC, removalGraphC);
        Set<T> residualGraphCTags = Sets.difference(nodeC.getGraphTags(), removalGraphCTags);

        logger.debug("At node " + nodeC.getId() + ": Cloned graph size reduced from  " + setOps.size(graphC) + " -> " + setOps.size(residualGraphC));

        BiMap<V, V> isoNewBC = HashBiMap.create(
                Maps.difference(nodeC.getTransIso(), removalTransIsoBC).entriesOnlyOnLeft());

        //BiMap<V, V> isoNewBC = HashBiMap.create(nodeC.getTransIso());
        //isoNewBC.entrySet().removeAll(isoBC.entrySet());
//                BiMap<V, V> deltaIso = HashBiMap.create(
//                        Maps.inte(iso, transBaseIso).entriesOnlyOnLeft());

        GraphIndexNode<K, G, V, T> newNodeC = createNode(residualGraphC, residualGraphCTags, isoNewBC);


//        GraphIndexNode<K, G, V, T> newNodeC = createNode(residualGraphC, residualGraphCTags, isoBC);
        //newNodeC.getKeys().addAll(nodeC.getKeys());


        // Then for each child: map the removal graph according to the child's iso
        for(GraphIndexNode<K, G, V, T> nodeD : nodeC.getChildren()) {

            BiMap<V, V> isoCD = nodeD.getTransIso();

            BiMap<V, V> newBaseIso = mapDomainVia(baseIso, isoCD);

//            GraphIsoMap removalGraphD = new GraphIsoMapImpl(removalGraphC, isoCD);
            G removalGraphD = setOps.applyIso(removalGraphC, newBaseIso); //isoCD);

            BiMap<V, V> removalTransIsoCD = mapDomainVia(removalTransIsoBC, isoCD);

            // NOTE Graph tags are unaffected by isomorphism
            Set<T> removalGraphDTags = removalGraphCTags;

            GraphIndexNode<K, G, V, T> cloneChild = cloneWithRemoval(nodeD, newBaseIso, removalTransIsoCD, removalGraphD, removalGraphDTags, writer);
            //deleteNode(child.getKey());
            newNodeC.appendChild(cloneChild);
        }

        long[] nodeIds = nodeC.getChildren().stream().mapToLong(GraphIndexNode::getId).toArray();
        for(Long nodeId : nodeIds) {
            deleteNode(nodeId);
        }



        return newNodeC;
    }

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
//
//        BiMap<X, X> result = src.entrySet().stream().collect(Collectors.toMap(
//                Entry::getKey,
//                e -> { X tmp = map.getOrDefault(e.getKey(), e.getKey()); System.out.println(e.getKey() + " :-> " + tmp); return tmp; },
//                (u, v) -> {
//                    throw new RuntimeException("should not hapen: " + src + " --- map: " + map);
//                },
//                HashBiMap::create));
//        return result;
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
    void findInsertPositions(Collection<InsertPosition<K, G, V, T>> out, GraphIndexNode<K, G, V, T> node, G insertGraph, Set<T> insertGraphTags, BiMap<V, V> baseIso, BiMap<V, V> latestIsoAB, boolean retrievalMode, boolean exactMatch, IndentedWriter writer) {

        writer.println("Finding insert position for user graph of size " + setOps.size(insertGraph));

        // Create the residual set of tags by removing the tags present on the current node from the graphTags
        Set<T> residualInsertGraphTags = Sets.difference(insertGraphTags, node.getGraphTags());


        boolean isSubsumed = false;

        boolean insertAtThisNode = false;


        // Candidate children for recursive lookup of the insert position
        // are those whose tags are subsets of the insertGraphTags
        writer.incIndent();
        Collection<GraphIndexNode<K, G, V, T>> candChildren =
                node.childIndex.getAllSubsetsOf(residualInsertGraphTags, false).keySet().stream()
                .map(nodeId -> node.idToChild.get(nodeId))
                .collect(Collectors.toList());

        //for(GraphIndexNode<K, G, N> child : node.getChildren()) {
        for(GraphIndexNode<K, G, V, T> child : candChildren) {

            G viewGraph = child.getValue();

            writer.println("Comparison with view graph of size " + setOps.size(viewGraph));
            //RDFDataMgr.write(System.out, viewGraph, RDFFormat.NTRIPLES);
            //System.out.println("under: " + viewGraph.getInToOut());

            // For every found isomorphism, check all children whether they are also isomorphic.
            writer.incIndent();
            int i = 0;

            // Before testing for isomorphy,
            // we need to remap the baseIso with the candidate node's transIso
            // (transIso captures the remapping of set/graph items when moving from the parent to the child node)
            // E.g. if the parent was {(?x a Foo)} and the child is {(?s a Bar)}, then the transIso could be ?x -> ?s
            // if the child node's full graph was { ?s a Foo ; a Bar }.
            BiMap<V, V> childTransIso = child.getTransIso();
            BiMap<V, V> transBaseIso = mapDomainVia(baseIso, childTransIso);

            //System.out.println("TRANS BASE ISO: " + childTransIso);
            //System.out.println("APPLIED TRANS BASE ISO: " + transBaseIso);

//            BiMap<V, V> ffs = transBaseIso;
//            if(setOps.size(viewGraph) == 9) {
//                System.out.println("Got debug iso: " + ffs);
//                //                ffs = HashBiMap.create();
//            }

            //baseIso.inverse()
            //Iterable<BiMap<V, V>> isos = isoMatcher.match(baseIso, viewGraph, insertGraph);
            Iterable<BiMap<V, V>> isos = isoMatcher.match(transBaseIso, viewGraph, insertGraph);
//            isos = Lists.newArrayList(isos);
//            System.out.println("Worked B!");
            for(BiMap<V, V> iso : isos) {
            //for(BiMap<Node, Node> iso : Lists.newArrayList(toIterable(QueryToJenaGraph.match(baseIso, viewGraph, insertGraph)))) {

                // TODO I tink the next 3 commented out lines are wrong and can be removed
                // Compute the differences between iso and transBaseIso
                // Thereby skip identity mappings
                // Map<V, V> deltaIsoTmp = Maps.difference(iso, transBaseIso).entriesOnlyOnLeft();

                // The difference is all non-identical mappings
                // FIXME We could exclude identical mappings already in the iso matcher
                BiMap<V, V> deltaIso = iso.entrySet().stream()
                        .filter(e -> !Objects.equals(e.getKey(), e.getValue()))
                        .collect(collectToBiMap(Entry::getKey, Entry::getValue));


                writer.println("Found match #" + ++i + ":");
                writer.incIndent();


                // We need to validate whether the mapping is compatible with the base mapping
                // E.g. if we insert [i1: { ?s ?p ?o }, i2: { ?x a Person }, i3: { ?y a Person ; label ?l}
                // Then there will be two isos from i1 to i3, but only one that is compatible with i2
                boolean isCompatible = MapUtils.isCompatible(iso, transBaseIso);
                if(!isCompatible) {
                    System.out.println("Incompatible:");
                    System.out.println("iso         : " + iso);
                    System.out.println("transBaseIso: " + transBaseIso);
                    //throw new RuntimeException("should not happen");
                    //insertAtThisNode = true;
                    continue;
                }

                // A graph is only subsumed if the found iso is compatible with the base iso
                isSubsumed = true;

                // Affected keys are the nodes of the view graph that were newly mapped by the iso
                // We implement a state-space-search approach here: We update the transBaseIso in place
                Set<V> affectedKeys = new HashSet<>(Sets.difference(iso.keySet(), transBaseIso.keySet()));


                //writer.println("From node " + node + " child " + child);
//                writer.println("baseIso     : " + baseIso);
//                writer.println("viewGraphIso: " + viewGraph.getInToOut());
                writer.println("affectedkeys: " + affectedKeys);
                writer.println("iso         : " + iso);
                writer.println("deltaIso    : " + deltaIso);

                affectedKeys.forEach(k -> transBaseIso.put(k, iso.get(k)));
                //writer.println("Contributed " + affectedKeys + " yielding iso mapping: " + iso);

                // iso: how to rename nodes of the view graph so it matches with the insert graph
                //Graph g = new GraphIsoMapImpl(viewGraph, iso);
                G g = setOps.applyIso(viewGraph, iso);

                G residualInsertGraph = setOps.difference(insertGraph, g);
                //Difference diff = new Difference(insertGraph, g);

                // now create the diff between the insert graph and mapped child graph
                writer.println("Diff " + residualInsertGraph + " has "+ setOps.size(residualInsertGraph) + " triples at depth " + writer.getUnitIndent());


                //Set<Object> tagDiff = Sets.difference(insertGraphTags, node.graphTags);


                // TODO optimize handling of empty diffs
                findInsertPositions(out, child, residualInsertGraph, residualInsertGraphTags, transBaseIso, deltaIso, retrievalMode, exactMatch, writer);

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
                InsertPosition<K, G, V, T> pos = new InsertPosition<>(node, insertGraph, residualInsertGraphTags, HashBiMap.create(baseIso), latestIsoAB);
                out.add(pos);
            }
        }
    }

    //@Override
    public GraphIndexNode<K, G, V, T> deleteNode(Long node) {
        GraphIndexNode<K, G, V, T> result = keyToNode.remove(node);
        if(result.getParent() != null) {
            result.getParent().removeChildById(node);
        }

        // todo: unlink the node from the parent

        //result.getP

        idToKeys.removeAll(node);
        //idToGraph.remove(node);

        return result;
        //return super.deleteNode(node);
    }

    /**
     * During the insert procedure, the insert graph is never renamed, because we want to figure out
     * how to remap existing nodes such they become a subgraph of the insertGraph.
     *
     * @param graph
     */
    void add(GraphIndexNode<K, G, V, T> node, K key, G insertGraph, Set<T> insertGraphTags, BiMap<V, V> baseIso, boolean forceInsert, IndentedWriter writer) {
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
        printTree(rootNode, IndentedWriter.stdout);
    }

    public void printTree(GraphIndexNode<K, G, V, T> node, IndentedWriter writer) {
        writer.println("" + node.getId() + " keys: " + idToKeys.get(node.getId()) + " --- tags: " + node.getGraphTags() + " --- transIso:" + node.getTransIso());
        writer.incIndent();
        for(GraphIndexNode<K, G, V, T> child : node.getChildren()) {
            printTree(child, writer);
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

    public static <T> Stream<T> breadthFirstStreamMulti(Collection<T> nodes, Function<T, Stream<T>> nodeToChildren) {
        Stream<T> result = Stream.concat(
                nodes.stream(),
                nodes.stream().flatMap(child -> breadthFirstStream(child, nodeToChildren)));

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
    public Map<K, G> loadGraphsInSubTree(Collection<GraphIndexNode<K, G, V, T>> startNodes) {
        Map<K, GraphIndexNode<K, G, V, T>> keyToNode = new HashMap<>();

        breadthFirstStreamMulti(startNodes, n -> n.getChildren().stream())
            .forEach(n -> {
                System.out.println("BFS: " + n);
                Set<K> keys = idToKeys.get(n.getId());
                for(K key : keys) {
                    keyToNode.computeIfAbsent(key, k -> n);
                }
            });

        // Now process the map; we can memoize graphs computed for nodes
        Map<K, G> result = keyToNode.entrySet().stream()
            .collect(Collectors.toMap(
                Entry::getKey,
                e -> assembleGraphAtNode(e.getValue(), (n) -> startNodes.contains(n))));

        for(Entry<K, G> e : result.entrySet()) {
            System.out.println("GRAPH OF: " + e.getKey());
            Graph<?, ?> g = (Graph<?, ?>)e.getValue();
            g.edgeSet().forEach(xxx -> System.out.println("edge: " + xxx));
        }

        return result;
    }

    /**
     * Get the list of parents, reverse it and apply transIso and union
     *
     * @param node
     * @return
     */
    public G assembleGraphAtNode(GraphIndexNode<K, G, V, T> destNode, Predicate<GraphIndexNode<K, G, V, T>> isStartNode) {

        // TODO Support memoization of assembledGraphs
        G result = isStartNode.test(destNode)
            ? destNode.getValue()
            : setOps.union(
                setOps.applyIso(assembleGraphAtNode(destNode.getParent(), isStartNode), destNode.getTransIso()),
                destNode.getValue());

        return result;
    }

    public static <T> void parentsOf(T node, Function<T, T> getParent) {

    }



    void performAdd(K key, InsertPosition<K, G, V, T> pos, boolean forceInsert, IndentedWriter writer) {
        GraphIndexNode<K, G, V, T> nodeA = pos.getNode();
        //Graph insertGraphIsoB = pos.getGraphIso();

        G residualInsertGraphB = pos.getResidualQueryGraph();
        Set<T> residualInsertGraphBTags = pos.getResidualQueryGraphTags();


        // If the insert graph is empty, just append the key to the insert node
        // i.e. do not create a child node
        if(isEmpty(residualInsertGraphB)) {
            //nodeA.getKeys().add(key);
            idToKeys.put(nodeA.getId(), key);
            return;
        }

        BiMap<V, V> isoAB = pos.getLatestIsoAB();
        BiMap<V, V> baseIsoAB = pos.getIso();

        // TODO isoAB should have an empty intersection with iso parentOfAtoA
        // Make a sanity check to ensure that

        // If nodeA does not have children, we just append nodeB
        // Otherwise, we need to do a more complex update procedure where we
        // re-insert every graph in the node's subtree
        GraphIndexNode<K, G, V, T> nodeB = createNode(residualInsertGraphB, residualInsertGraphBTags, isoAB);
        if(forceInsert || nodeA.childIndex.isEmpty()) {
            Long nodeBId = nodeB.getId();
            nodeA.appendChild(nodeB);
            idToKeys.put(nodeBId, key);
        } else {
            // Create the node that will replace nodeA, and
            // create and append the insert node as a child
            TagMap<Long, T> tagMap = new TagMapSetTrie<>(tagComparator);
            GraphIndexNode<K, G, V, T> replacementNodeA = new GraphIndexNode<>(nodeA.getParent(), nodeA.getId(), nodeA.getTransIso(), nodeA.getValue(), nodeA.getGraphTags(), tagMap);

            replacementNodeA.appendChild(nodeB);
            idToKeys.put(nodeB.getId(), key);

            // The candidate children or those whose tag sets are a super set
            // of that of the insert graph
//            Set<GraphIndexNode<K, G, V, T>> directCandChildren =
//                    nodeA.childIndex.getAllSupersetsOf(residualInsertGraphBTags, false).keySet().stream()
//                    .map(nodeId -> nodeA.idToChild.get(nodeId))
//                    .collect(Collectors.toCollection(Sets::newIdentityHashSet));

            Set<GraphIndexNode<K, G, V, T>> directCandChildren = nodeA.childIndex.getAllSupersetsOf(Collections.emptySet(), false).keySet().stream()
                    .map(nodeId -> nodeA.idToChild.get(nodeId))
                    .collect(Collectors.toCollection(Sets::newIdentityHashSet));

            List<GraphIndexNode<K, G, V, T>> nonDirectCandChildren = nodeA.idToChild.values().stream()
                    .filter(node -> !directCandChildren.contains(node))
                    .collect(Collectors.toList());

            // Iterate all children whose graphTags are a super set of the given one
            // Thereby recursively updating the lookup tag set with the graphTags covered by a node
            Stream<GraphIndexNode<K, G, V, T>> candChildren = lookupProvidedChildrenByTags(
                    directCandChildren.stream(),
                    residualInsertGraphBTags,
                    (node, tags) -> Sets.difference(tags, node.getGraphTags()),
                    (node, tags) -> node.childIndex.getAllSupersetsOf(tags, false)
                            .keySet().stream().map(nodeId -> nodeA.idToChild.get(nodeId))
                    );


//            Collection<GraphIndexNode<K, G, V, T>> tmp = candChildren.collect(Collectors.toList());
//
//            candChildren = tmp.stream();
//
//            // For every key in the sub-tree, collect one representative
//            // residual graph.
//            Map<K, G> keyToGraph = new HashMap<>();
//            Multimap<K, K> altKeys = HashMultimap.create();
//            candChildren.forEach(node -> {
//                // Pick one key as the representative key and use the others as alt keys
//                Collection<K> keys = new HashSet<>(idToKeys.get(node.getId()));
//                idToKeys.removeAll(node.getId());
//
//                if(!keys.isEmpty()) {
//                    K repKey = keys.iterator().next();
//                    for(K altKey : keys) {
//                        if(!Objects.equals(repKey, altKey)) {
//                            altKeys.put(repKey, altKey);
//                        }
//                    }
//
//                    // For the repKey, build the graph
//                    // The baseGraph is this node's parent keyGraph
//                    Collection<K> parentKeys = keys;///idToKeys.get(node.getId());
//                    K parentRepKey = Iterables.getFirst(parentKeys, null);
//                    G baseGraph = keyToGraph.get(parentRepKey);
//                    G keyGraph = baseGraph == null
//                            ? node.getValue()
//                            : setOps.union(setOps.applyIso(baseGraph, node.transIso), node.graph)
//                            ;
//
//                    keyToGraph.put(repKey, keyGraph);
//                }
//            });

            // Order the graphs by size
            Map<K, G> keyToGraph = loadGraphsInSubTree(directCandChildren);

            System.out.println("Found these graphs in subtree of candidate children: " + keyToGraph.keySet());

            List<Entry<K, G>> keyGraphs = new ArrayList<>(keyToGraph.entrySet());
            Collections.sort(keyGraphs, (a, b) -> setOps.size(a.getValue()) - setOps.size(b.getValue()));

            // Perform additions
            for(Entry<K, G> keyGraph : keyGraphs) {
                K k = keyGraph.getKey();
                G insertGraph = keyGraph.getValue();
                Set<T> insertGraphTags = extractGraphTagsWrapper(insertGraph);
                //BiMap<V, V> baseIso = HashBiMap.create();

                add(replacementNodeA, k, insertGraph, insertGraphTags, baseIsoAB, true, writer);
            }

            // Append all unaffected non-candidate children
            for(GraphIndexNode<K, G, V, T> node : nonDirectCandChildren) {
                node.setParent(null);
                replacementNodeA.appendChild(node);
            }

            // Replace the node and update keys
            nodeA.childIndex = replacementNodeA.childIndex;
            nodeA.idToChild = replacementNodeA.idToChild;

            // For every child, set the parent to nodeA
            replacementNodeA.idToChild.values().forEach(n -> {n.setParent(null); n.setParent(nodeA); });

            //idToKeys.putAll(nodeAId, nodeAKeys);
            /*
            Set<K> keys = idToKeys.get(nodeA.getId());

            deleteNode(nodeA.getId());
            nodeA.getParent().appendChild(replacementNodeA);

            for(K k : keys) {
                idToKeys.put(replacementNodeA.getId(), k);
            }
            */
        }
    }


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
//    void performAddBullshit(K key, InsertPosition<K, G, V, T> pos, IndentedWriter writer) {
//        GraphIndexNode<K, G, V, T> nodeA = pos.getNode();
//        //Graph insertGraphIsoB = pos.getGraphIso();
//
//        G residualInsertGraphB = pos.getResidualQueryGraph();
//        Set<T> residualInsertGraphBTags = pos.getResidualQueryGraphTags();
//
//
//        // If the insert graph is empty, just append the key to the insert node
//        // i.e. do not create a child node
//        if(isEmpty(residualInsertGraphB)) {
//            nodeA.getKeys().add(key);
//            return;
//        }
//
//
//
//        BiMap<V, V> isoAB = pos.getLatestIsoAB();
//        BiMap<V, V> baseIsoAB = pos.getIso();
//
//        // TODO isoAB should have an empty intersection with iso parentOfAtoA
//        // Make a sanity check to ensure that
//
//        GraphIndexNode<K, G, V, T> nodeB = createNode(residualInsertGraphB, residualInsertGraphBTags, isoAB);
//        nodeB.getKeys().add(key);
//
//        writer.println("Insert attempt of user graph of size " + setOps.size(residualInsertGraphB));
////        RDFDataMgr.write(System.out, insertGraph, RDFFormat.NTRIPLES);
////        System.out.println("under: " + currentIso);
//
//        // If the addition is not on a leaf node, check if we subsume anything
//        boolean isSubsumed = nodeA.getChildren().stream().filter(c -> !c.getKeys().contains(key)).count() == 0;//;isEmpty(); //{false};
//
//
//        // TODO We must not insert to nodes where we just inserted
//
//        // Make a copy of the baseIso, as it is transient due to state space search
//        //GraphIsoMap gim = new GraphIsoMapImpl(insertGraph, HashBiMap.create(baseIso));
//
//        //boolean wasAdded = false;
//
//        // If the insertGraph was not subsumed,
//        // check if it subsumes any of the other children
//        // for example { ?s ?p ?o } may not be subsumed by an existing child, but it will subsume any other children
//        // use clusters
//        // add it as a new child
//        if(!isSubsumed) {
//            writer.println("We are not subsumed, but maybe we subsume");
////            GraphIndexNode<K> nodeB = null;//createNode(graphIso);//new GraphIndexNode<K>(graphIso);
//
//
//            writer.incIndent();
//            //for(GraphIndexNode child : children) {
//            //Iterator<GraphIndexNode<K>> it = nodeA.getChildren().iterator();//children.listIterator();
//            Iterator<GraphIndexNode<K, G, V, T>> it = new ArrayList<>(nodeA.getChildren()).iterator();
//            while(it.hasNext()) {
//                GraphIndexNode<K, G, V, T> nodeC = it.next();
//                G viewGraphC = nodeC.getValue();
//                Set<T> viewGraphCTags = nodeC.getGraphTags();
//
//                writer.println("Comparison with view graph of size " + setOps.size(viewGraphC));
////                RDFDataMgr.write(System.out, viewGraph, RDFFormat.NTRIPLES);
////                System.out.println("under: " + currentIso);
//
//                // For every found isomorphism, check all children whether they are also isomorphic.
//                writer.incIndent();
//                int i = 0;
//
//                boolean isSubsumedC = false;
////baseIso: ?x -> ?y, transIso: ?x -> ?z => ?y -> ?z
//                BiMap<V, V> transIsoAC = nodeC.getTransIso();
//                BiMap<V, V> transBaseIsoBC = chain(baseIsoAB.inverse(), transIsoAC);
//                //mapDomainVia(nodeC.getTransIso(), );
//
//
//                //Iterable<BiMap<V, V>> isosBC = isoMatcher.match(baseIso.inverse(), residualInsertGraphB, viewGraphC);//QueryToJenaGraph.match(baseIso.inverse(), residualInsertGraphB, viewGraphC).collect(Collectors.toSet());
//                Iterable<BiMap<V, V>> isosBC = isoMatcher.match(transBaseIsoBC, residualInsertGraphB, viewGraphC);//QueryToJenaGraph.match(baseIso.inverse(), residualInsertGraphB, viewGraphC).collect(Collectors.toSet());
////                isosBC = Lists.newArrayList(isosBC);
////                System.out.println("Worked A!");
//                for(BiMap<V, V> isoBC : isosBC) {
//                    isSubsumedC = true;
//                    writer.println("Detected subsumption #" + ++i + " with iso: " + isoBC);
//                    writer.incIndent();
//
//                    // We found an is from B to C, where there was a prior iso from A to C
//                    // This means, we need to update the transIso of C as if we were coming from B instead of A
//
//                    // TODO FUCK! This isoGraph object may be a reason to keep the original graph and the iso in a combined graph object
//                    //nodeB = nodeB == null ? createNode(residualInsertGraphB, isoAB) : nodeB;
//                    G mappedResidualInsertGraphC = setOps.applyIso(residualInsertGraphB, isoBC);
//                    Set<T> mappedResidualInsertGraphCTags = residualInsertGraphBTags;
//                    G removalGraphC = setOps.intersect(mappedResidualInsertGraphC, viewGraphC);
//
//                    Set<T> removalGraphCTags = Sets.intersection(mappedResidualInsertGraphCTags, viewGraphCTags);
//
//
//                    BiMap<V, V> transIsoBAC = chain(baseIsoAB.inverse(), transIsoAC);
//
//                    // The isoBC must be a subset of transIsoAC (because A subgraphOf B subgraphOf C)
//                    // Get the mappings that are in common, so we can subtract them
//                    BiMap<V, V> removalIsoBC = HashBiMap.create(Maps.difference(isoBC, transIsoBAC).entriesInCommon());
//
//
//                     // BiMap<V, V> deltaIsoABC = HashBiMap.create(Maps.difference(isoBC, transIsoAC).entriesInCommon());
//                    //System.out.println("deltaIsoBC: " + deltaIsoBC);
//
//                    //BiMap<V, V> removalTransIsoBC = HashBiMap.create(Maps.difference(isoBC, transIsoAC).entriesInCommon());
//
//
////                    BiMap<V, V> newTransIsoC = mapDomainVia(nodeC.getTransIso(), isoBC);
//                    //BiMap<V, V> newTransIsoC = mapDomainVia(nodeC.getTransIso(), isoBC);
//                    //System.out.println("NewTransIsoC: " + newTransIsoC);
//
//
//
//                    GraphIndexNode<K, G, V, T> newChildC = cloneWithRemoval(nodeC, isoBC, removalIsoBC, removalGraphC, removalGraphCTags, writer);
//                    nodeB.appendChild(newChildC);//add(newChild, baseIso, writer);
//
//
//                    writer.decIndent();
//                }
//
//                if(isSubsumedC) {
//                    deleteNode(nodeC.getKey());
//                }
//
//
////                if(nodeB != null) {
////                    //it.remove();
////
////                    //nodeB.getKeys().add(key);
////
////                    writer.println("A node was subsumed and therefore removed");
////                    //wasAdded = true;
////                    // not sure if this remove works
////                }
//                writer.decIndent();
//
//            }
//            writer.decIndent();
//
//        }
//
//        // If nothing was subsumed, add it to this node
//        //if(!wasAdded) {
//            writer.println("Attached graph of size " + setOps.size(residualInsertGraphB) + " to node " + nodeA);
//            nodeA.appendChild(nodeB);
//            //GraphIndexNode<K> target = createNode(residualInsertGraphB, baseIso);
//            //target.getKeys().add(key);
//            //nodeA.appendChild(target);
//        //}
//    }

}


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
