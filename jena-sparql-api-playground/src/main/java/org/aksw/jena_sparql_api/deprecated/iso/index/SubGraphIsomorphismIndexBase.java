package org.aksw.jena_sparql_api.deprecated.iso.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collections.MapUtils;
import org.aksw.commons.collections.multimaps.BiHashMultimap;
import org.aksw.commons.collections.trees.ReclaimingSupplier;
import org.aksw.commons.graph.index.jena.transform.QueryToJenaGraph;
import org.aksw.commons.jena.graph.GraphIsoMap;
import org.aksw.commons.jena.graph.GraphIsoMapImpl;
import org.aksw.commons.jena.graph.GraphVar;
import org.aksw.commons.jena.graph.GraphVarImpl;
import org.aksw.jena_sparql_api.utils.SetGraph;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;

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
public class SubGraphIsomorphismIndexBase<K>
    implements SubGraphIsomorphismIndex<K>
//    extends LabeledTree<Long, //GraphIndexNode<K>>
{
    protected long root;
    protected GraphIndexNode<K> rootNode;
    protected Supplier<Long> idSupplier;
    protected Map<Long, GraphIndexNode<K>> keyToNode = new HashMap<>();

    public static SubGraphIsomorphismIndex<Node> create() {
        int i[] = {0};
        Supplier<Node> idSupplier = () -> NodeFactory.createURI("http://index.node/id" + i[0]++);
        SubGraphIsomorphismIndex<Node> result = new SubGraphIsomorphismIndexBase<>(idSupplier);
        return result;
    }

    public SubGraphIsomorphismIndexBase(Supplier<K> keySupplier) {
        super();
        this.keySupplier = keySupplier;

        long i[] = {0};
        idSupplier = new ReclaimingSupplier<>(() -> i[0]++);


        rootNode = createNode(new GraphVarImpl(), HashBiMap.create());
        //rootNode = createNode(new GraphIsoMapImpl(new GraphVarImpl(), HashBiMap.create()));
        root = rootNode.getKey();

    }

    Map<Long, GraphIsoMap> idToGraph = new HashMap<>();


//    long nextId[] = {0};
//    protected ReclaimingSupplier<Long> idSupplier = new ReclaimingSupplier<>(() -> nextId[0]++ );


//    protected GraphIndexNode<K> root = new GraphIndexNode<K>(new GraphIsoMapImpl(new GraphVarImpl(), HashBiMap.create()));
//
    protected Supplier<K> keySupplier;
//    protected Map<Long, GraphIndexNode<K>> IdToNode = new HashMap<>();
//    protected ReversibleMap<Long, Long> childToParent = new ReversibleMapImpl<>();

    protected BiHashMultimap<Long, K> idToKeys = new BiHashMultimap<>();



    /* (non-Javadoc)
     * @see org.aksw.jena_sparql_api.deprecated.iso.index.SubGraphIsomorphismIndex#removeKey(java.lang.Object)
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
    protected GraphIndexNode<K> createNode(Graph graph, BiMap<Node, Node> transIso) {
        Long id = idSupplier.get();
        GraphIndexNode<K> result = new GraphIndexNode<K>(null, id);
        keyToNode.put(id, result);

        result.setValue(graph);
        result.setTransIso(transIso);

        return result;
    }



    /* (non-Javadoc)
     * @see org.aksw.jena_sparql_api.deprecated.iso.index.SubGraphIsomorphismIndex#add(org.apache.jena.graph.Graph)
     */
    @Override
    public K add(Graph graph) {
        K key = keySupplier.get();

        put(key, graph);

        return key;
    }


    /* (non-Javadoc)
     * @see org.aksw.jena_sparql_api.deprecated.iso.index.SubGraphIsomorphismIndex#lookup(org.apache.jena.graph.Graph, boolean)
     */
    @Override
    public Multimap<K, InsertPositionOld<K>> lookup(Graph queryGraph, boolean exactMatch) {

        Collection<InsertPositionOld<K>> positions = new LinkedList<>();
        findInsertPositions(positions, rootNode, queryGraph, HashBiMap.create(), HashBiMap.create(), true, exactMatch, IndentedWriter.stderr);

        Multimap<K, InsertPositionOld<K>> result = HashMultimap.create();
        System.out.println("Lookup result candidates: " + positions.size());
        for(InsertPositionOld<K> pos : positions) {
            // Match with the children

            //System.out.println("Node " + pos.node + " with keys " + pos.node.getKeys() + " iso: " + pos.getGraphIso().getInToOut());
            for(K key : pos.node.getKeys()) {
                result.put(key, pos);
            }
        }
        return result;
    }
//
//    public Map<K, ProblemNeighborhoodAware<BiMap<Var, Var>, Var>> lookupStream(Graph queryGraph, boolean exactMatch) {
//        Multimap<K, InsertPositionOld<K>> matches = lookup(queryGraph, exactMatch);
//
//        Map<K, ProblemNeighborhoodAware<BiMap<Var, Var>, Var>> result = matches.asMap().entrySet().stream().collect(Collectors.toMap(
//            e -> e.getKey(),
//            e -> SparqlViewMatcherQfpcIso.createCompound(e.getValue())));
//
//
//        return result;
//    }




    public Multimap<K, BiMap<Node, Node>> lookupFlat(Graph queryGraph, boolean exactMatch) {

        Collection<InsertPositionOld<K>> positions = new LinkedList<>();
        findInsertPositions(positions, rootNode, queryGraph, HashBiMap.create(), HashBiMap.create(), true, exactMatch, IndentedWriter.stderr);

        Multimap<K, BiMap<Node, Node>> result = HashMultimap.create();
        System.out.println("Lookup result candidates: " + positions.size());
        for(InsertPositionOld<K> pos : positions) {
            // Match with the children

            //System.out.println("Node " + pos.node + " with keys " + pos.node.getKeys() + " iso: " + pos.getGraphIso().getInToOut());
            for(K key : pos.node.getKeys()) {
                result.put(key, pos.getIso());
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.aksw.jena_sparql_api.deprecated.iso.index.SubGraphIsomorphismIndex#put(K, org.apache.jena.graph.Graph)
     */
    @Override
    public K put(K key, Graph graph) {
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
    GraphIndexNode<K> cloneWithRemoval(GraphIndexNode<K> nodeC, BiMap<Node, Node> isoBC, Graph removalGraphC, IndentedWriter writer) { //BiMap<Node, Node> isoBC, Graph residualInsertGraphB,
        Graph graphC = nodeC.getValue();

        Graph residualGraphC = difference(graphC, removalGraphC);
        writer.println("Cloned graph size reduced from  " + graphC.size() + " -> " + residualGraphC.size());

        GraphIndexNode<K> newNodeC = createNode(residualGraphC, isoBC);
        newNodeC.getKeys().addAll(nodeC.getKeys());


        // Then for each child: map the removal graph according to the child's iso
        for(GraphIndexNode<K> child : nodeC.getChildren()) {

            BiMap<Node, Node> isoCD = child.getTransIso();
            GraphIsoMap removalGraphD = new GraphIsoMapImpl(removalGraphC, isoCD);

            GraphIndexNode<K> cloneChild = cloneWithRemoval(child, isoCD, removalGraphD, writer);
            //deleteNode(child.getKey());
            newNodeC.appendChild(cloneChild);
        }

        long[] nodeIds = nodeC.getChildren().stream().mapToLong(GraphIndexNode::getKey).toArray();
        for(Long nodeId : nodeIds) {
            deleteNode(nodeId);
        }



        return newNodeC;
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
    public static BiMap<Node, Node> mapDomainVia(BiMap<Node, Node> src, BiMap<Node, Node> map) {
        BiMap<Node, Node> result = src.entrySet().stream().collect(Collectors.toMap(
                e -> map.getOrDefault(e.getKey(), e.getKey()),
                e -> e.getValue(),
                (u, v) -> {
                    throw new RuntimeException("should not hapen: " + src + " --- map: " + map);
                },
                HashBiMap::create));
        return result;
    }


    public static GraphVar difference(Graph ag, Graph bg) {
        Set<Triple> as = new SetGraph(ag);
        Set<Triple> bs = new SetGraph(bg);
        Set<Triple> c = Sets.difference(as, bs);

        GraphVar result = new GraphVarImpl();
        GraphUtil.add(result, c.iterator());

        return result;
    }

    public static GraphVar intersection(Graph ag, Graph bg) {
        Set<Triple> as = new SetGraph(ag);
        Set<Triple> bs = new SetGraph(bg);
        Set<Triple> c = Sets.intersection(as, bs);

        GraphVar result = new GraphVarImpl();
        GraphUtil.add(result, c.iterator());

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
    void findInsertPositions(Collection<InsertPositionOld<K>> out, GraphIndexNode<K> node, Graph insertGraph, BiMap<Node, Node> rawBaseIso, BiMap<Node, Node> latestIsoAB, boolean retrievalMode, boolean exactMatch, IndentedWriter writer) {
        BiMap<Node, Node> transIso = node.getTransIso();//.getValue().getInToOut();

        // Map the image of the iso through the view graph iso
        BiMap<Node, Node> baseIso = mapDomainVia(rawBaseIso, transIso);


        writer.println("Finding insert position for user graph of size " + insertGraph.size());
        //RDFDataMgr.write(System.out, insertGraph, RDFFormat.NTRIPLES);
        //System.out.println("under raw: " + rawBaseIso);

        boolean isSubsumed = false;

        writer.incIndent();
        for(GraphIndexNode<K> child : node.getChildren()) {

            Graph viewGraph = child.getValue();

            writer.println("Comparison with view graph of size " + viewGraph.size());
            //RDFDataMgr.write(System.out, viewGraph, RDFFormat.NTRIPLES);
            //System.out.println("under: " + viewGraph.getInToOut());

            // For every found isomorphism, check all children whether they are also isomorphic.
            //
            writer.incIndent();
            int i = 0;
            //baseIso.inverse()
            Iterable<BiMap<Node, Node>> isos = null; //QueryToJenaGraph.match(baseIso, viewGraph, insertGraph).collect(Collectors.toSet());
            for(BiMap<Node, Node> iso : isos) {
            //for(BiMap<Node, Node> iso : Lists.newArrayList(toIterable(QueryToJenaGraph.match(baseIso, viewGraph, insertGraph)))) {

                isSubsumed = true;

                writer.println("Found match #" + ++i + ":");
                writer.incIndent();

                boolean isCompatible = MapUtils.isCompatible(iso, baseIso);
                if(!isCompatible) {
                    throw new RuntimeException("should not happen");
                }


                Set<Node> affectedKeys = new HashSet<>(Sets.difference(iso.keySet(), baseIso.keySet()));
                //writer.println("From node " + node + " child " + child);
//                writer.println("baseIso     : " + baseIso);
//                writer.println("viewGraphIso: " + viewGraph.getInToOut());
                writer.println("iso         : " + iso);

                affectedKeys.forEach(k -> baseIso.put(k, iso.get(k)));
                //writer.println("Contributed " + affectedKeys + " yielding iso mapping: " + iso);

                // iso: how to rename nodes of the view graph so it matches with the insert graph
                Graph g = new GraphIsoMapImpl(viewGraph, iso);

                GraphVar diff = difference(insertGraph, g);
                //Difference diff = new Difference(insertGraph, g);

                // now create the diff between the insert graph and mapped child graph
                writer.println("Diff " + diff + " has "+ diff.size() + " triples at depth " + writer.getUnitIndent());



                // TODO optimize handling of empty diffs
                findInsertPositions(out, child, diff, baseIso, iso, retrievalMode, exactMatch, writer);

                affectedKeys.forEach(baseIso::remove);

                writer.decIndent();
            }
            writer.decIndent();
        }
        writer.decIndent();

        if(!isSubsumed || retrievalMode) {

            if(!exactMatch || insertGraph.isEmpty()) {
                writer.println("Marking location for insert");
                //System.out.println("keys at node: " + node.getKeys() + " - " + node);
                // Make a copy of the baseIso, as it is transient due to state space search
                InsertPositionOld<K> pos = new InsertPositionOld<>(node, insertGraph, HashBiMap.create(baseIso), latestIsoAB);
                out.add(pos);
            }
        }
    }

    //@Override
    public GraphIndexNode<K> deleteNode(Long node) {
        GraphIndexNode<K> result = keyToNode.remove(node);
        if(result.getParent() != null) {
            result.getParent().removeChildById(node);
        }

        // todo: unlink the node from the parent

        //result.getP

        idToKeys.removeAll(node);
        idToGraph.remove(node);

        return result;
        //return super.deleteNode(node);
    }

    /**
     * During the insert procedure, the insert graph is never renamed, because we want to figure out
     * how to remap existing nodes such they become a subgraph of the insertGraph.
     *
     * @param graph
     */
    void add(K key, Graph insertGraph, BiMap<Node, Node> baseIso, IndentedWriter writer) {
        // The insert graph must be larger than the node Graph

        Collection<InsertPositionOld<K>> positions = new LinkedList<>();
        findInsertPositions(positions, rootNode, insertGraph, baseIso, HashBiMap.create(), false, false, writer);

//        positions.forEach(p -> {
//            System.out.println("Insert pos: " + p.getNode().getKey() + " --- " + p.getIso());
//        });

        for(InsertPositionOld<K> pos : positions) {
            performAdd(key, pos, writer);
        }
    }

    public void printTree() {
        printTree(rootNode, IndentedWriter.stdout);
    }

    public void printTree(GraphIndexNode<K> node, IndentedWriter writer) {
        writer.println("" + node.getKey() + " keys: " + node.getKeys());
        writer.incIndent();
        for(GraphIndexNode<K> child : node.getChildren()) {
            printTree(child, writer);
        }
        writer.decIndent();
    }

    void performAdd(K key, InsertPositionOld<K> pos, IndentedWriter writer) {
        GraphIndexNode<K> nodeA = pos.getNode();
        //Graph insertGraphIsoB = pos.getGraphIso();

        Graph residualInsertGraphB = pos.getResidualQueryGraph();


        // If the insert graph is empty, just append the key to the insert node
        // i.e. do not create a child node
        if(residualInsertGraphB.isEmpty()) {
            nodeA.getKeys().add(key);
            return;
        }



        BiMap<Node, Node> isoAB = pos.getLatestIsoAB();
        BiMap<Node, Node> baseIso = pos.getIso();

        GraphIndexNode<K> nodeB = createNode(residualInsertGraphB, isoAB);
        nodeB.getKeys().add(key);

        writer.println("Insert attempt of user graph of size " + residualInsertGraphB.size());
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
            Iterator<GraphIndexNode<K>> it = new ArrayList<>(nodeA.getChildren()).iterator();
            while(it.hasNext()) {
                GraphIndexNode<K> nodeC = it.next();
                Graph viewGraphC = nodeC.getValue();

                writer.println("Comparison with view graph of size " + viewGraphC.size());
//                RDFDataMgr.write(System.out, viewGraph, RDFFormat.NTRIPLES);
//                System.out.println("under: " + currentIso);

                // For every found isomorphism, check all children whether they are also isomorphic.
                writer.incIndent();
                int i = 0;

                boolean isSubsumedC = false;
                Iterable<BiMap<Node, Node>> isosBC = null; //QueryToJenaGraph.match(baseIso.inverse(), residualInsertGraphB, viewGraphC).collect(Collectors.toSet());
                for(BiMap<Node, Node> isoBC : isosBC) {
                    isSubsumedC = true;
                    writer.println("Detected subsumption #" + ++i + " with iso: " + isoBC);
                    writer.incIndent();

                    // TODO FUCK! This isoGraph object may be a reason to keep the original graph and the iso in a combined graph object
                    //nodeB = nodeB == null ? createNode(residualInsertGraphB, isoAB) : nodeB;
                    GraphIsoMap mappedResidualInsertGraphC = new GraphIsoMapImpl(residualInsertGraphB, isoBC);
                    Graph removalGraphC = intersection(mappedResidualInsertGraphC, viewGraphC);
                    GraphIndexNode<K> newChildC = cloneWithRemoval(nodeC, isoBC, removalGraphC, writer);

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
            writer.println("Attached graph of size " + residualInsertGraphB.size() + " to node " + nodeA);
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
