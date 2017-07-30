package org.aksw.jena_sparql_api.iso.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collections.MapUtils;
import org.aksw.commons.collections.multimaps.BiHashMultimap;
import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.aksw.commons.collections.set_trie.TagMapSetTrie;
import org.aksw.commons.collections.trees.ReclaimingSupplier;
import org.apache.jena.atlas.io.IndentedWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 *
 *
 *
 * @author raven
 *
 * @param <K>
 */
public abstract class SubGraphIsomorphismIndexBase<K, G, V> implements SubGraphIsomorphismIndex<K, G, V>
//    extends LabeledTree<Long, //GraphIndexNode<K>>
{
    private static final Logger logger = LoggerFactory.getLogger(SubGraphIsomorphismIndexBase.class);

    // Optional function that extracts a set of static features from a graph
    // Typically this is the set of static node and/or edge labels
    // The feature set must be consistent with the isomorphism relation:
    // If there is a subgraph iso from '(n)eedle' to '(h)aystack, then
    // the features of n must be a subset of that of h
    // existsIso(n, h) implies features(n) subsetOf features(h)
    //protected Function<G, Collection<?>> graphToStaticFeatures;
    //protected abstract Collection<?> extractGraphTags(G graph);

    protected Function<? super G, Collection<?>> extractGraphTags;

    protected SetOps<G, V> setOps;
//    public abstract G createSet(); //new GraphVarImpl()
//    public abstract G applyIso(G set, BiMap<N, N> iso); // new GraphIsoMapImpl(removalGraphC, isoCD)
//    public abstract int size(G set);
//    public abstract G difference(G baseSet, G removalSet);
//    public abstract G intersection(G baseSet, G removalSet);

    protected Set<Object> extractGraphTagsWrapper(G graph) {
        Collection<?> tmp = extractGraphTags.apply(graph);
        Set<Object> result = tmp.stream().collect(Collectors.toSet());
        return result;
    }


    public abstract Iterable<BiMap<V, V>> match(BiMap<V, V> baseIso, G a, G b); // QueryToJenaGraph.match(baseIso, viewGraph, insertGraph).collect(Collectors.toSet());




    protected long root;
    protected GraphIndexNode<K, G, V> rootNode;
    protected Supplier<Long> idSupplier;
    protected Map<Long, GraphIndexNode<K, G, V>> keyToNode = new HashMap<>();


    public SubGraphIsomorphismIndexBase(
            Supplier<K> keySupplier,
            SetOps<G, V> setOps,
            Function<? super G, Collection<?>> extractGraphTags
            ) {
        super();
        this.keySupplier = keySupplier;
        this.setOps = setOps;
        this.extractGraphTags = extractGraphTags;

        long i[] = {0};
        idSupplier = new ReclaimingSupplier<>(() -> i[0]++);


        rootNode = createNode(setOps.createNew(), Collections.emptySet(), HashBiMap.create());
        //rootNode = createNode(new GraphIsoMapImpl(new GraphVarImpl(), HashBiMap.create()));
        root = rootNode.getKey();

    }


    //Map<Long, GraphIsoMap> idToGraph = new HashMap<>();


//    long nextId[] = {0};
//    protected ReclaimingSupplier<Long> idSupplier = new ReclaimingSupplier<>(() -> nextId[0]++ );


//    protected GraphIndexNode<K> root = new GraphIndexNode<K>(new GraphIsoMapImpl(new GraphVarImpl(), HashBiMap.create()));
//
    protected Supplier<K> keySupplier;
//    protected Map<Long, GraphIndexNode<K>> IdToNode = new HashMap<>();
//    protected ReversibleMap<Long, Long> childToParent = new ReversibleMapImpl<>();

    protected IBiSetMultimap<Long, K> idToKeys = new BiHashMultimap<>();



    /* (non-Javadoc)
     * @see org.aksw.jena_sparql_api.iso.index.SubGraphIsomorphismIndex#removeKey(java.lang.Object)
     */
    @Override
    public void removeKey(Object key) {
        // Copy id set to avoid concurrent modification
        //Set<Long> ids = idToKeys.getInverse().get(key);
        Set<Long> ids = new HashSet<>(idToKeys.getInverse().get(key));
        for(Long id : ids) {
            keyToNode.get(id).getKeys().remove(key);
            //deleteNode(id);
        }
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
    protected GraphIndexNode<K, G, V> createNode(G graph, Set<Object> graphTags, BiMap<V, V> transIso) {
        Long id = idSupplier.get();

//        Set<Object> graphTags = extractGraphTagsWrapper(graph);

        GraphIndexNode<K, G, V> result = new GraphIndexNode<K, G, V>(null, id, transIso, graph, graphTags, new TagMapSetTrie<>());
        keyToNode.put(id, result);

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


    /* (non-Javadoc)
     * @see org.aksw.jena_sparql_api.iso.index.SubGraphIsomorphismIndex#lookup(G, boolean)
     */
    //@Override
    public Multimap<K, InsertPosition<K, G, V>> lookup(G queryGraph, boolean exactMatch) {

        Set<Object> queryGraphTags = extractGraphTagsWrapper(queryGraph);

        Collection<InsertPosition<K, G, V>> positions = new LinkedList<>();
        findInsertPositions(positions, rootNode, queryGraph, queryGraphTags, HashBiMap.create(), HashBiMap.create(), true, exactMatch, IndentedWriter.stderr);

        Multimap<K, InsertPosition<K, G, V>> result = HashMultimap.create();
        logger.debug("Lookup result candidates: " + positions.size());
        for(InsertPosition<K, G, V> pos : positions) {
            // Match with the children

            //System.out.println("Node " + pos.node + " with keys " + pos.node.getKeys() + " iso: " + pos.getGraphIso().getInToOut());
            for(K key : pos.node.getKeys()) {
                result.put(key, pos);
            }
        }
        return result;
    }





    public Multimap<K, BiMap<V, V>> lookupFlat(G queryGraph, boolean exactMatch) {

        Set<Object> queryGraphTags = extractGraphTagsWrapper(queryGraph);


        Collection<InsertPosition<K, G, V>> positions = new LinkedList<>();
        findInsertPositions(positions, rootNode, queryGraph, queryGraphTags, HashBiMap.create(), HashBiMap.create(), true, exactMatch, IndentedWriter.stderr);

        Multimap<K, BiMap<V, V>> result = HashMultimap.create();
        logger.debug("Lookup result candidates: " + positions.size());
        for(InsertPosition<K, G, V> pos : positions) {
            // Match with the children

            //System.out.println("Node " + pos.node + " with keys " + pos.node.getKeys() + " iso: " + pos.getGraphIso().getInToOut());
            for(K key : pos.node.getKeys()) {
                result.put(key, pos.getIso());
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.aksw.jena_sparql_api.iso.index.SubGraphIsomorphismIndex#put(K, G)
     */
    @Override
    public K put(K key, G graph) {
        add(key, graph, HashBiMap.create(), IndentedWriter.stderr);

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
    GraphIndexNode<K, G, V> cloneWithRemoval(GraphIndexNode<K, G, V> nodeC, BiMap<V, V> isoBC, G removalGraphC, Set<Object> removalGraphCTags, IndentedWriter writer) { //BiMap<Node, Node> isoBC, Graph residualInsertGraphB,
        G graphC = nodeC.getValue();

        G residualGraphC = setOps.difference(graphC, removalGraphC);
        Set<Object> residualGraphCTags = Sets.difference(nodeC.getGraphTags(), removalGraphCTags);

        logger.debug("Cloned graph size reduced from  " + setOps.size(graphC) + " -> " + setOps.size(residualGraphC));

        GraphIndexNode<K, G, V> newNodeC = createNode(residualGraphC, residualGraphCTags, isoBC);
        newNodeC.getKeys().addAll(nodeC.getKeys());


        // Then for each child: map the removal graph according to the child's iso
        for(GraphIndexNode<K, G, V> child : nodeC.getChildren()) {

            BiMap<V, V> isoCD = child.getTransIso();
//            GraphIsoMap removalGraphD = new GraphIsoMapImpl(removalGraphC, isoCD);
            G removalGraphD = setOps.applyIso(removalGraphC, isoCD);

            // NOTE Graph tags are unaffected by isomorphism
            Set<Object> removalGraphDTags = removalGraphCTags;

            GraphIndexNode<K, G, V> cloneChild = cloneWithRemoval(child, isoCD, removalGraphD, removalGraphDTags, writer);
            //deleteNode(child.getKey());
            newNodeC.appendChild(cloneChild);
        }

        long[] nodeIds = nodeC.getChildren().stream().mapToLong(GraphIndexNode::getKey).toArray();
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
    public static <N> BiMap<N, N> mapDomainVia(BiMap<N, N> src, BiMap<N, N> map) {
        BiMap<N, N> result = src.entrySet().stream().collect(Collectors.toMap(
                e -> map.getOrDefault(e.getKey(), e.getKey()),
                e -> e.getValue(),
                (u, v) -> {
                    throw new RuntimeException("should not hapen: " + src + " --- map: " + map);
                },
                HashBiMap::create));
        return result;
    }


    /**
     * For a given insertGraph, find all nodes in the tree at which insert has to occurr.
     *
     *
     * @param out
     * @param node
     * @param insertGraph
     * @param rawBaseIso
     * @param retrievalMode false: only return leaf nodes of insertion, true: return all encountered nodes
     * @param writer
     */
    void findInsertPositions(Collection<InsertPosition<K, G, V>> out, GraphIndexNode<K, G, V> node, G insertGraph, Set<Object> insertGraphTags, BiMap<V, V> rawBaseIso, BiMap<V, V> latestIsoAB, boolean retrievalMode, boolean exactMatch, IndentedWriter writer) {
        BiMap<V, V> transIso = node.getTransIso();//.getValue().getInToOut();

        // Map the image of the iso through the view graph iso
        BiMap<V, V> baseIso = mapDomainVia(rawBaseIso, transIso);

        // Create the residual set of tags by removing the tags present on the current node from the graphTags
        Set<Object> residualInsertGraphTags = Sets.difference(insertGraphTags, node.getGraphTags());


        writer.println("Finding insert position for user graph of size " + setOps.size(insertGraph));
        //RDFDataMgr.write(System.out, insertGraph, RDFFormat.NTRIPLES);
        //System.out.println("under raw: " + rawBaseIso);

        boolean isSubsumed = false;

        writer.incIndent();
        Collection<GraphIndexNode<K, G, V>> candChildren =
                node.childIndex.getAllSupersetsOf(residualInsertGraphTags, false).keySet().stream()
                .map(nodeId -> node.idToChild.get(nodeId))
                .collect(Collectors.toList());

        //for(GraphIndexNode<K, G, N> child : node.getChildren()) {
        for(GraphIndexNode<K, G, V> child : candChildren) {

            G viewGraph = child.getValue();

            writer.println("Comparison with view graph of size " + setOps.size(viewGraph));
            //RDFDataMgr.write(System.out, viewGraph, RDFFormat.NTRIPLES);
            //System.out.println("under: " + viewGraph.getInToOut());

            // For every found isomorphism, check all children whether they are also isomorphic.
            //
            writer.incIndent();
            int i = 0;
            //baseIso.inverse()
            Iterable<BiMap<V, V>> isos = match(baseIso, viewGraph, insertGraph);
            for(BiMap<V, V> iso : isos) {
            //for(BiMap<Node, Node> iso : Lists.newArrayList(toIterable(QueryToJenaGraph.match(baseIso, viewGraph, insertGraph)))) {

                isSubsumed = true;

                writer.println("Found match #" + ++i + ":");
                writer.incIndent();

                // TODO Eventually remove this validation or turn it into an assertion
                boolean isCompatible = MapUtils.isCompatible(iso, baseIso);
                if(!isCompatible) {
                    throw new RuntimeException("should not happen");
                }


                Set<V> affectedKeys = new HashSet<>(Sets.difference(iso.keySet(), baseIso.keySet()));
                //writer.println("From node " + node + " child " + child);
//                writer.println("baseIso     : " + baseIso);
//                writer.println("viewGraphIso: " + viewGraph.getInToOut());
                writer.println("iso         : " + iso);

                affectedKeys.forEach(k -> baseIso.put(k, iso.get(k)));
                //writer.println("Contributed " + affectedKeys + " yielding iso mapping: " + iso);

                // iso: how to rename nodes of the view graph so it matches with the insert graph
                //Graph g = new GraphIsoMapImpl(viewGraph, iso);
                G g = setOps.applyIso(viewGraph, iso);

                G graphDiff = setOps.difference(insertGraph, g);
                //Difference diff = new Difference(insertGraph, g);

                // now create the diff between the insert graph and mapped child graph
                writer.println("Diff " + graphDiff + " has "+ setOps.size(graphDiff) + " triples at depth " + writer.getUnitIndent());


                //Set<Object> tagDiff = Sets.difference(insertGraphTags, node.graphTags);


                // TODO optimize handling of empty diffs
                findInsertPositions(out, child, graphDiff, residualInsertGraphTags, baseIso, iso, retrievalMode, exactMatch, writer);

                affectedKeys.forEach(baseIso::remove);

                writer.decIndent();
            }
            writer.decIndent();
        }
        writer.decIndent();

        if(!isSubsumed || retrievalMode) {

            if(!exactMatch || isEmpty(insertGraph)) {
                writer.println("Marking location for insert");
                //System.out.println("keys at node: " + node.getKeys() + " - " + node);
                // Make a copy of the baseIso, as it is transient due to state space search
                InsertPosition<K, G, V> pos = new InsertPosition<>(node, insertGraph, insertGraphTags, HashBiMap.create(baseIso), latestIsoAB);
                out.add(pos);
            }
        }
    }

    //@Override
    public GraphIndexNode<K, G, V> deleteNode(Long node) {
        GraphIndexNode<K, G, V> result = keyToNode.remove(node);
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
    void add(K key, G insertGraph, BiMap<V, V> baseIso, IndentedWriter writer) {
        // The insert graph must be larger than the node Graph

        Set<Object> insertGraphTags = extractGraphTagsWrapper(insertGraph);

        Collection<InsertPosition<K, G, V>> positions = new LinkedList<>();
        findInsertPositions(positions, rootNode, insertGraph, insertGraphTags, baseIso, HashBiMap.create(), false, false, writer);

//        positions.forEach(p -> {
//            System.out.println("Insert pos: " + p.getNode().getKey() + " --- " + p.getIso());
//        });

        for(InsertPosition<K, G, V> pos : positions) {
            performAdd(key, pos, writer);
        }
    }

    public void printTree() {
        printTree(rootNode, IndentedWriter.stdout);
    }

    public void printTree(GraphIndexNode<K, G, V> node, IndentedWriter writer) {
        writer.println("" + node.getKey() + " keys: " + node.getKeys() + " --- " + node.getGraphTags());
        writer.incIndent();
        for(GraphIndexNode<K, G, V> child : node.getChildren()) {
            printTree(child, writer);
        }
        writer.decIndent();
    }

    void performAdd(K key, InsertPosition<K, G, V> pos, IndentedWriter writer) {
        GraphIndexNode<K, G, V> nodeA = pos.getNode();
        //Graph insertGraphIsoB = pos.getGraphIso();

        G residualInsertGraphB = pos.getResidualQueryGraph();
        Set<Object> residualInsertGraphBTags = pos.getResidualQueryGraphTags();


        // If the insert graph is empty, just append the key to the insert node
        // i.e. do not create a child node
        if(isEmpty(residualInsertGraphB)) {
            nodeA.getKeys().add(key);
            return;
        }



        BiMap<V, V> isoAB = pos.getLatestIsoAB();
        BiMap<V, V> baseIso = pos.getIso();

        GraphIndexNode<K, G, V> nodeB = createNode(residualInsertGraphB, residualInsertGraphBTags, isoAB);
        nodeB.getKeys().add(key);

        writer.println("Insert attempt of user graph of size " + setOps.size(residualInsertGraphB));
//        RDFDataMgr.write(System.out, insertGraph, RDFFormat.NTRIPLES);
//        System.out.println("under: " + currentIso);

        // If the addition is not on a leaf node, check if we subsume anything
        boolean isSubsumed = nodeA.getChildren().stream().filter(c -> !c.getKeys().contains(key)).count() == 0;//;isEmpty(); //{false};


        // TODO We must not insert to nodes where we just inserted

        // Make a copy of the baseIso, as it is transient due to state space search
        //GraphIsoMap gim = new GraphIsoMapImpl(insertGraph, HashBiMap.create(baseIso));

        //boolean wasAdded = false;

        // If the insertGraph was not subsumed,
        // check if it subsumes any of the other children
        // for example { ?s ?p ?o } may not be subsumed by an existing child, but it will subsume any other children
        // use clusters
        // add it as a new child
        if(!isSubsumed) {
            writer.println("We are not subsumed, but maybe we subsume");
//            GraphIndexNode<K> nodeB = null;//createNode(graphIso);//new GraphIndexNode<K>(graphIso);


            writer.incIndent();
            //for(GraphIndexNode child : children) {
            //Iterator<GraphIndexNode<K>> it = nodeA.getChildren().iterator();//children.listIterator();
            Iterator<GraphIndexNode<K, G, V>> it = new ArrayList<>(nodeA.getChildren()).iterator();
            while(it.hasNext()) {
                GraphIndexNode<K, G, V> nodeC = it.next();
                G viewGraphC = nodeC.getValue();
                Set<Object> viewGraphCTags = nodeC.getGraphTags();

                writer.println("Comparison with view graph of size " + setOps.size(viewGraphC));
//                RDFDataMgr.write(System.out, viewGraph, RDFFormat.NTRIPLES);
//                System.out.println("under: " + currentIso);

                // For every found isomorphism, check all children whether they are also isomorphic.
                writer.incIndent();
                int i = 0;

                boolean isSubsumedC = false;
                Iterable<BiMap<V, V>> isosBC = match(baseIso.inverse(), residualInsertGraphB, viewGraphC);//QueryToJenaGraph.match(baseIso.inverse(), residualInsertGraphB, viewGraphC).collect(Collectors.toSet());
                for(BiMap<V, V> isoBC : isosBC) {
                    isSubsumedC = true;
                    writer.println("Detected subsumption #" + ++i + " with iso: " + isoBC);
                    writer.incIndent();

                    // TODO FUCK! This isoGraph object may be a reason to keep the original graph and the iso in a combined graph object
                    //nodeB = nodeB == null ? createNode(residualInsertGraphB, isoAB) : nodeB;
                    G mappedResidualInsertGraphC = setOps.applyIso(residualInsertGraphB, isoBC);
                    Set<Object> mappedResidualInsertGraphCTags = residualInsertGraphBTags;
                    G removalGraphC = setOps.intersect(mappedResidualInsertGraphC, viewGraphC);

                    Set<Object> removalGraphCTags = Sets.intersection(mappedResidualInsertGraphCTags, viewGraphCTags);

                    GraphIndexNode<K, G, V> newChildC = cloneWithRemoval(nodeC, isoBC, removalGraphC, removalGraphCTags, writer);

                    nodeB.appendChild(newChildC);//add(newChild, baseIso, writer);


                    writer.decIndent();
                }

                if(isSubsumedC) {
                    deleteNode(nodeC.getKey());
                }


//                if(nodeB != null) {
//                    //it.remove();
//
//                    //nodeB.getKeys().add(key);
//
//                    writer.println("A node was subsumed and therefore removed");
//                    //wasAdded = true;
//                    // not sure if this remove works
//                }
                writer.decIndent();

            }
            writer.decIndent();

        }

        // If nothing was subsumed, add it to this node
        //if(!wasAdded) {
            writer.println("Attached graph of size " + setOps.size(residualInsertGraphB) + " to node " + nodeA);
            nodeA.appendChild(nodeB);
            //GraphIndexNode<K> target = createNode(residualInsertGraphB, baseIso);
            //target.getKeys().add(key);
            //nodeA.appendChild(target);
        //}
    }

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
