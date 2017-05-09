package org.aksw.jena_sparql_api.iso.index;

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

import org.aksw.combinatorics.solvers.ProblemNeighborhoodAware;
import org.aksw.commons.collections.MapUtils;
import org.aksw.commons.collections.multimaps.BiHashMultimap;
import org.aksw.commons.collections.trees.ReclaimingSupplier;
import org.aksw.jena_sparql_api.jgrapht.SparqlViewMatcherQfpcIso;
import org.aksw.jena_sparql_api.jgrapht.transform.GraphIsoMap;
import org.aksw.jena_sparql_api.jgrapht.transform.GraphIsoMapImpl;
import org.aksw.jena_sparql_api.jgrapht.transform.GraphVar;
import org.aksw.jena_sparql_api.jgrapht.transform.GraphVarImpl;
import org.aksw.jena_sparql_api.jgrapht.transform.QueryToJenaGraph;
import org.aksw.jena_sparql_api.utils.SetGraph;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.compose.Difference;
import org.apache.jena.graph.compose.Intersection;
import org.apache.jena.sparql.core.Var;

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
public class SubGraphIsomorphismIndex<K>
//    extends LabeledTree<Long, //GraphIndexNode<K>>
{
    protected long root;
    protected GraphIndexNode<K> rootNode;
    protected Supplier<Long> idSupplier;
    protected Map<Long, GraphIndexNode<K>> keyToNode = new HashMap<>();

    public static SubGraphIsomorphismIndex<Node> create() {
        int i[] = {0};
        Supplier<Node> idSupplier = () -> NodeFactory.createURI("http://index.node/id" + i[0]++);
        SubGraphIsomorphismIndex<Node> result = new SubGraphIsomorphismIndex<>(idSupplier);
        return result;
    }

    public SubGraphIsomorphismIndex(Supplier<K> keySupplier) {
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



    /**
     * Insert the graph and allocate a fresh, unused, id
     *
     * @param graph
     */
    public K add(Graph graph) {
        K key = keySupplier.get();

        put(key, graph);

        return key;
    }


    /**
     * For each key, returns sets of objects each comprising:
     * - the index node, holding the residual index graph
     * - the isomorphism from the residual index graph to the residual query graph
     * - the residual query graph
     *
     *
     *
     * @param queryGraph
     * @return
     */
    public Multimap<K, InsertPosition<K>> lookup(Graph queryGraph, boolean exactMatch) {

        Collection<InsertPosition<K>> positions = new LinkedList<>();
        findInsertPositions(positions, rootNode, queryGraph, HashBiMap.create(), true, exactMatch, IndentedWriter.stderr);

        Multimap<K, InsertPosition<K>> result = HashMultimap.create();
        System.out.println("Lookup result candidates: " + positions.size());
        for(InsertPosition<K> pos : positions) {
            // Match with the children

            //System.out.println("Node " + pos.node + " with keys " + pos.node.getKeys() + " iso: " + pos.getGraphIso().getInToOut());
            for(K key : pos.node.getKeys()) {
                result.put(key, pos);
            }
        }
        return result;
    }

    public Map<K, ProblemNeighborhoodAware<BiMap<Var, Var>, Var>> lookupStream(Graph queryGraph, boolean exactMatch) {
        Multimap<K, InsertPosition<K>> matches = lookup(queryGraph, exactMatch);

        Map<K, ProblemNeighborhoodAware<BiMap<Var, Var>, Var>> result = matches.asMap().entrySet().stream().collect(Collectors.toMap(
            e -> e.getKey(),
            e -> SparqlViewMatcherQfpcIso.createCompound(e.getValue())));


        return result;
    }




    public Multimap<K, BiMap<Node, Node>> lookupFlat(Graph queryGraph, boolean exactMatch) {

        Collection<InsertPosition<K>> positions = new LinkedList<>();
        findInsertPositions(positions, rootNode, queryGraph, HashBiMap.create(), true, exactMatch, IndentedWriter.stderr);

        Multimap<K, BiMap<Node, Node>> result = HashMultimap.create();
        System.out.println("Lookup result candidates: " + positions.size());
        for(InsertPosition<K> pos : positions) {
            // Match with the children

            //System.out.println("Node " + pos.node + " with keys " + pos.node.getKeys() + " iso: " + pos.getGraphIso().getInToOut());
            for(K key : pos.node.getKeys()) {
                result.put(key, pos.getIso());
            }
        }
        return result;
    }

    /**
     * Insert a graph pattern with a specific key, thereby replacing any existing one having this key already
     *
     * @param key
     * @param graph
     * @return
     */
    public K put(K key, Graph graph) {
        add(key, graph, HashBiMap.create(), IndentedWriter.stderr);

        return null;
    }


    public static <T> Iterable<T> toIterable(Stream<T> stream) {
        Iterable<T> result = () -> stream.iterator();
        return result;
    }

    /**
     * Clones a sub tree thereby removing the triples in the removal graph
     *
     *
     * @param removalGraph
     * @param writer
     * @return
     */
    GraphIndexNode<K> cloneWithRemoval(GraphIndexNode<K> nodeC, BiMap<Node, Node> isoAB, Graph removalGraph, IndentedWriter writer) {
        Graph graphC = nodeC.getValue();
        BiMap<Node, Node> isoAC = nodeC.getTransIso();//graphC.getInToOut();

        GraphIsoMap mappedRemovalGraph = new GraphIsoMapImpl(removalGraph, isoAC);

        //System.out.println("REMOVAL");
        //RDFDataMgr.write(System.out, mappedRemovalGraph, RDFFormat.NTRIPLES);
        //System.out.println("DATA");
        //RDFDataMgr.write(System.out, graphIso, RDFFormat.NTRIPLES);

        // Create a clone of the node graph
        // Alternatively, create a Difference view
        //Graph cloneGraph = GraphFactory.createDefaultGraph();
        //
        Graph cloneGraph = new Difference(graphC, mappedRemovalGraph);
        writer.println("Cloned graph size reduced from  " + graphC.size() + " -> " + cloneGraph.size());


        BiMap<Node, Node> isoBC = mapDomainVia(isoAC, isoAB.inverse());
        writer.println("iso to subsumed node: " + isoBC);

        // Remove from the prior node iso the parts now covered by the iso


        GraphIndexNode<K> result = createNode(cloneGraph, isoBC);
        //node.getKeys()
        result.getKeys().addAll(nodeC.getKeys());


        // Then for each child: map the removal graph according to the child's iso
        for(GraphIndexNode<K> child : nodeC.getChildren()) {

            GraphIndexNode<K> cloneChild = cloneWithRemoval(child, isoBC, mappedRemovalGraph, writer);
            deleteNode(child.getKey());
            result.appendChild(cloneChild);
        }



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
    void findInsertPositions(Collection<InsertPosition<K>> out, GraphIndexNode<K> node, Graph insertGraph, BiMap<Node, Node> rawBaseIso, boolean retrievalMode, boolean exactMatch, IndentedWriter writer) {
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
            Iterable<BiMap<Node, Node>> isos = QueryToJenaGraph.match(baseIso, viewGraph, insertGraph).collect(Collectors.toSet());
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
                findInsertPositions(out, child, diff, baseIso, retrievalMode, exactMatch, writer);

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
                InsertPosition<K> pos = new InsertPosition<>(node, insertGraph, HashBiMap.create(baseIso));
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

        Collection<InsertPosition<K>> positions = new LinkedList<>();
        findInsertPositions(positions, rootNode, insertGraph, baseIso, false, false, writer);

//        positions.forEach(p -> {
//            System.out.println("Insert pos: " + p.getNode().getKey() + " --- " + p.getIso());
//        });

        for(InsertPosition<K> pos : positions) {
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

    void performAdd(K key, InsertPosition<K> pos, IndentedWriter writer) {
        GraphIndexNode<K> nodeA = pos.getNode();
        //Graph insertGraphIsoB = pos.getGraphIso();
        Graph insertGraphB = pos.getResidualQueryGraph();
        BiMap<Node, Node> baseIso = pos.getIso();

        writer.println("Insert attempt of user graph of size " + insertGraphB.size());

        // If the insert graph is empty, just append the key to the insert node
        // i.e. do not create a child node
        if(insertGraphB.isEmpty()) {
            nodeA.getKeys().add(key);
            return;
        }

//        RDFDataMgr.write(System.out, insertGraph, RDFFormat.NTRIPLES);
//        System.out.println("under: " + currentIso);

        // If the addition is not on a leaf node, check if we subsume anything
        boolean isSubsumed = nodeA.getChildren().stream().filter(c -> !c.getKeys().contains(key)).count() == 0;//;isEmpty(); //{false};


        // TODO We must not insert to nodes where we just inserted

        // Make a copy of the baseIso, as it is transient due to state space search
        //GraphIsoMap gim = new GraphIsoMapImpl(insertGraph, HashBiMap.create(baseIso));

        boolean wasAdded = false;

        // If the insertGraph was not subsumed,
        // check if it subsumes any of the other children
        // for example { ?s ?p ?o } may not be subsumed by an existing child, but it will subsume any other children
        // use clusters
        // add it as a new child
        if(!isSubsumed) {
            writer.println("We are not subsumed, but maybe we subsume");
            GraphIndexNode<K> nodeB = null;//createNode(graphIso);//new GraphIndexNode<K>(graphIso);


            writer.incIndent();
            //for(GraphIndexNode child : children) {
            Iterator<GraphIndexNode<K>> it = nodeA.getChildren().iterator();//children.listIterator();
            while(it.hasNext()) {
                GraphIndexNode<K> nodeC = it.next();
                Graph viewGraphC = nodeC.getValue();

                writer.println("Comparison with view graph of size " + viewGraphC.size());
//                RDFDataMgr.write(System.out, viewGraph, RDFFormat.NTRIPLES);
//                System.out.println("under: " + currentIso);

                // For every found isomorphism, check all children whether they are also isomorphic.
                writer.incIndent();
                int i = 0;

//
//                Iterable<BiMap<Node, Node>> isoTmp = Lists.newArrayList(toIterable(QueryToJenaGraph.match(baseIso.inverse(), insertGraph, viewGraph)));
//
//                GraphVar ga = new GraphVarImpl();
//                //insertGraph.find(Node.ANY, Node.ANY, Node.ANY).forEachRemaining(ga::add);
//                GraphUtil.addInto(ga, insertGraph);
//                ga.find(Node.ANY, Node.ANY, Var.alloc("ao")).forEachRemaining(x -> System.out.println(x));
//                GraphVar gb = new GraphVarImpl();
//                viewGraph.find(Node.ANY, Node.ANY, Node.ANY).forEachRemaining(gb::add);
//                //GraphUtil.addInto(gb, viewGraph);
//                insertGraph = ga;
//                viewGraph = new GraphIsoMapImpl(gb, HashBiMap.create());

                Iterable<BiMap<Node, Node>> isos = QueryToJenaGraph.match(baseIso.inverse(), insertGraphB, viewGraphC).collect(Collectors.toSet());
                for(BiMap<Node, Node> isoAB : isos) {
                    writer.println("Detected subsumption #" + ++i);
                    writer.println("  with iso: " + isoAB);
                    writer.incIndent();

                    // TODO FUCK! This isoGraph object may be a reason to keep the original graph and the iso in a combined graph object
                    GraphIsoMap mappedInsertGraph = new GraphIsoMapImpl(insertGraphB, isoAB);
//                    System.out.println("Remapped insert via " + iso);
//                    RDFDataMgr.write(System.out, insertGraphX, RDFFormat.NTRIPLES);
//                    System.out.println("---");

                    //Difference retain = new Difference(viewGraph, insertGraphX);

                    // The part which is duplicated between the insert graph and the view
                    // is subject to removal
                    Intersection removalGraph = new Intersection(mappedInsertGraph, viewGraphC);

                    // Allocate root before child to give it a lower id for cosmetics
                    //nodeB = nodeB == null ? createNode(mappedInsertGraph) : nodeB;
                    nodeB = nodeB == null ? createNode(insertGraphB, isoAB) : nodeB;


                    GraphIndexNode<K> newChild = cloneWithRemoval(nodeC, isoAB, removalGraph, writer);
                    nodeB.appendChild(newChild);//add(newChild, baseIso, writer);

                    writer.decIndent();
                }

                if(nodeB != null) {
                    //it.remove();
                    deleteNode(nodeC.getKey());
                    nodeA.appendChild(nodeB);
                    nodeB.getKeys().add(key);

                    writer.println("A node was subsumed and therefore removed");
                    wasAdded = true;
                    // not sure if this remove works
                }
                writer.decIndent();

            }
            writer.decIndent();

        }

        // If nothing was subsumed, add it to this node
        if(!wasAdded) {
            writer.println("Attached graph of size " + insertGraphB.size() + " to node " + nodeA);
            GraphIndexNode<K> target = createNode(insertGraphB, baseIso);
            target.getKeys().add(key);
            nodeA.appendChild(target);
        }
    }

}