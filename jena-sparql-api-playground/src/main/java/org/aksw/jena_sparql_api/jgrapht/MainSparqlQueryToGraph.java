package org.aksw.jena_sparql_api.jgrapht;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JFrame;

import org.aksw.commons.collections.MapUtils;
import org.aksw.commons.collections.multimaps.BiHashMultimap;
import org.aksw.commons.collections.trees.LabeledNodeImpl;
import org.aksw.commons.collections.trees.LabeledTree;
import org.aksw.commons.collections.trees.ReclaimingSupplier;
import org.aksw.commons.util.strings.StringPrettyComparator;
import org.aksw.jena_sparql_api.concept_cache.op.OpExtConjunctiveQuery;
import org.aksw.jena_sparql_api.jgrapht.transform.GraphIsoMap;
import org.aksw.jena_sparql_api.jgrapht.transform.GraphIsoMapImpl;
import org.aksw.jena_sparql_api.jgrapht.transform.GraphVar;
import org.aksw.jena_sparql_api.jgrapht.transform.GraphVarImpl;
import org.aksw.jena_sparql_api.jgrapht.transform.QueryToGraphVisitor;
import org.aksw.jena_sparql_api.jgrapht.transform.QueryToJenaGraph;
import org.aksw.jena_sparql_api.jgrapht.wrapper.PseudoGraphJenaGraph;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.jena_sparql_api.views.index.SparqlViewMatcherOpImpl;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.compose.Difference;
import org.apache.jena.graph.compose.Intersection;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.graph.GraphFactory;
import org.jgraph.JGraph;
import org.jgrapht.ext.JGraphModelAdapter;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.JGraphLayout;
import com.jgraph.layout.hierarchical.JGraphHierarchicalLayout;


class ExtendedQueryToGraphVisitor
    extends QueryToGraphVisitor
{
    public ExtendedQueryToGraphVisitor() {
        super();
    }

//    public ExtendedQueryToGraphVisitor(Graph graph, Supplier<Node> nodeSupplier) {
//        super(graph, nodeSupplier);
//    }

    public ExtendedQueryToGraphVisitor(Supplier<Node> nodeSupplier) {
        super(nodeSupplier);
    }

    @Override
    public void visit(OpExt op) {
        if(op instanceof OpExtConjunctiveQuery) {
            ((OpExtConjunctiveQuery) op).getQfpc().toOp().visit(this);
        }
    }

}


class InsertPosition<K> {
    protected GraphIndexNode<K> node;
    protected GraphIsoMap graphIso;

    public InsertPosition(GraphIndexNode<K> node, GraphIsoMap graphIso) {
        super();
        this.node = node;
        this.graphIso = graphIso;
    }

    public GraphIndexNode<K> getNode() {
        return node;
    }

    public GraphIsoMap getGraphIso() {
        return graphIso;
    }

    @Override
    public String toString() {
        return "InsertPosition [node=" + node + ", graphIso=" + graphIso.size() + "]";
    }
}

/**
 *
 *
 * FIXME Materialize if nesting of views gets too deep
 * FIXME Cluster graphs by size (or maybe this is separate indices?) - we don't want to compute isomorphisms from { ?s ?p ?o } to a BGP with 100 TPs
 *
 * Materialization vs recomputation of iso-mappings
 *
 * We check whether a graph being inserted is subsumed by other graphs in regard to isomorphism.
 * Insert graph = graph being inserted
 * Node graph = graph at the current index node
 * As there can be several mappings between in insert graph and the node graph, we can always re-compute the set of iso mappings
 * based on the path to the root.
 *
 *
 *
 *
 * @author raven
 *
 */
class GraphIndexNode<K>
    extends LabeledNodeImpl<Long, GraphIndexNode<K>, GraphIndex<K>>
{
    public GraphIndexNode(GraphIndex<K> tree, Long id) {
        super(tree, id);
    }

    public GraphIsoMap getValue() {
        return tree.idToGraph.get(id);
    }

    public GraphIsoMap setValue(GraphIsoMap graphIso) {
        return tree.idToGraph.put(id, graphIso);
    }

    public Set<K> getKeys() {
        return tree.idToKeys.get(id);
    }

//    public Set<K> setKeys() {
//        return tree.idToKeys.pu
//    }

}

class GraphIndex<K>
    extends LabeledTree<Long, GraphIndexNode<K>>
{
    public static GraphIndex<Node> create() {
        int i[] = {0};
        Supplier<Node> idSupplier = () -> NodeFactory.createURI("http://index.node/id" + i[0]++);
        GraphIndex<Node> result = new GraphIndex<>(idSupplier);
        return result;
    }

    public GraphIndex(Supplier<K> keySupplier) {
        super();
        this.keySupplier = keySupplier;

        long i[] = {0};
        idSupplier = new ReclaimingSupplier<>(() -> i[0]++);


        rootNode = createNode(new GraphIsoMapImpl(new GraphVarImpl(), HashBiMap.create()));
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



    public void removeNodesByKey(K key) {

    }

    public void removeNodeById(Long id) {

    }


    /**
     * Create a detached node
     *
     * @param graphIso
     * @return
     */
    protected GraphIndexNode<K> createNode(GraphIsoMap graphIso) {
        Long id = idSupplier.get();
        GraphIndexNode<K> result = new GraphIndexNode<K>(this, id);
        keyToNode.put(id, result);

        result.setValue(graphIso);

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

    public Multimap<K, Map<Node, Node>> lookup(Graph graph) {

        Collection<InsertPosition<K>> positions = new LinkedList<>();
        findInsertPositions(positions, rootNode, graph, HashBiMap.create(), true, IndentedWriter.stderr);

        Multimap<K, Map<Node, Node>> result = HashMultimap.create();
        System.out.println("Lookup result candidates: " + positions.size());
        for(InsertPosition<K> pos : positions) {
            // Match with the children

            //System.out.println("Node " + pos.node + " with keys " + pos.node.getKeys() + " iso: " + pos.getGraphIso().getInToOut());
            for(K key : pos.node.getKeys()) {
                result.put(key, pos.graphIso.getInToOut());
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
    K put(K key, Graph graph) {
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
        GraphIsoMap graphC = nodeC.getValue();
        BiMap<Node, Node> isoAC = graphC.getInToOut();

        GraphIsoMap mappedRemovalGraph = new GraphIsoMapImpl(removalGraph, isoAC);

        //System.out.println("REMOVAL");
        //RDFDataMgr.write(System.out, mappedRemovalGraph, RDFFormat.NTRIPLES);
        //System.out.println("DATA");
        //RDFDataMgr.write(System.out, graphIso, RDFFormat.NTRIPLES);

        // Create a clone of the node graph
        // Alternatively, create a Difference view
        //Graph cloneGraph = GraphFactory.createDefaultGraph();
        //
        Graph cloneGraph = new Difference(graphC.getWrapped(), mappedRemovalGraph);
        writer.println("Cloned graph size reduced from  " + graphC.size() + " -> " + cloneGraph.size());


        BiMap<Node, Node> isoBC = mapDomainVia(isoAC, isoAB.inverse());
        writer.println("iso to subsumed node: " + isoBC);

        // Remove from the prior node iso the parts now covered by the iso


        GraphIndexNode<K> result = createNode(new GraphIsoMapImpl(cloneGraph, isoBC));
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
                    throw new RuntimeException("should not hapen");
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
    void findInsertPositions(Collection<InsertPosition<K>> out, GraphIndexNode<K> node, Graph insertGraph, BiMap<Node, Node> rawBaseIso, boolean retrievalMode, IndentedWriter writer) {
        BiMap<Node, Node> transIso = node.getValue().getInToOut();

        // Map the image of the iso through the view graph iso
        BiMap<Node, Node> baseIso = mapDomainVia(rawBaseIso, transIso);


        writer.println("Finding insert position for user graph of size " + insertGraph.size());
        RDFDataMgr.write(System.out, insertGraph, RDFFormat.NTRIPLES);
        //System.out.println("under raw: " + rawBaseIso);

        boolean isSubsumed = false;

        writer.incIndent();
        for(GraphIndexNode<K> child : node.getChildren()) {

            GraphIsoMap viewGraph = child.getValue();

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
                writer.println("From node " + node + " child " + child);
//                writer.println("baseIso     : " + baseIso);
//                writer.println("viewGraphIso: " + viewGraph.getInToOut());
                writer.println("iso         : " + iso);

                affectedKeys.forEach(k -> baseIso.put(k, iso.get(k)));
                //writer.println("Contributed " + affectedKeys + " yielding iso mapping: " + iso);

                // iso: how to rename nodes of the view graph so it matches with the insert graph
                Graph g = new GraphIsoMapImpl(viewGraph, iso);

                Difference diff = new Difference(insertGraph, g);

                // now create the diff between the insert graph and mapped child graph
                writer.println("Diff has " + diff.size() + " triples at depth " + writer.getUnitIndent());
                writer.println("Diff: " + diff);


                // TODO optimize handling of empty diffs
                findInsertPositions(out, child, diff, baseIso, retrievalMode, writer);

                affectedKeys.forEach(baseIso::remove);

                writer.decIndent();
            }
            writer.decIndent();
        }
        writer.decIndent();

        if(!isSubsumed || retrievalMode) {
            // Make a copy of the baseIso, as it is transient due to state space search
            GraphIsoMap gim = new GraphIsoMapImpl(insertGraph, HashBiMap.create(rawBaseIso));
            InsertPosition<K> pos = new InsertPosition<>(node, gim);
            out.add(pos);
        }
    }

    @Override
    public GraphIndexNode<K> deleteNode(Long node) {
        idToKeys.removeAll(node);
        idToGraph.remove(node);

        return super.deleteNode(node);
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
        findInsertPositions(positions, rootNode, insertGraph, baseIso, false, writer);

        for(InsertPosition<K> pos : positions) {
            performAdd(key, pos, writer);
        }
    }

    void performAdd(K key, InsertPosition<K> pos, IndentedWriter writer) {
        GraphIndexNode<K> nodeA = pos.getNode();
        GraphIsoMap insertGraphIsoB = pos.getGraphIso();
        Graph insertGraphB = insertGraphIsoB.getWrapped();
        BiMap<Node, Node> baseIso = insertGraphIsoB.getInToOut();

        writer.println("Insert attempt of user graph of size " + insertGraphB.size());
//        RDFDataMgr.write(System.out, insertGraph, RDFFormat.NTRIPLES);
//        System.out.println("under: " + currentIso);

        // If the addition is not on a leaf node, check if we subsume anything
        boolean isSubsumed = nodeA.getChildren().isEmpty(); //{false};


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
                GraphIsoMap viewGraphC = nodeC.getValue();

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

                    GraphIsoMap mappedInsertGraph = new GraphIsoMapImpl(insertGraphB, isoAB);
//                    System.out.println("Remapped insert via " + iso);
//                    RDFDataMgr.write(System.out, insertGraphX, RDFFormat.NTRIPLES);
//                    System.out.println("---");

                    //Difference retain = new Difference(viewGraph, insertGraphX);

                    // The part which is duplicated between the insert graph and the view
                    // is subject to removal
                    Intersection removalGraph = new Intersection(mappedInsertGraph, viewGraphC);

                    // Allocate root before child to give it a lower id for cosmetics
                    nodeB = nodeB == null ? createNode(mappedInsertGraph) : nodeB;

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
            writer.println("Attached graph of size " + insertGraphIsoB.size() + " to node " + nodeA);
            GraphIndexNode<K> target = createNode(insertGraphIsoB);
            target.getKeys().add(key);
            nodeA.appendChild(target);
        }
    }

}



public class MainSparqlQueryToGraph {

    public static <K, V> Map<K, V> prettify(Map<? extends K, ? extends V> map) {
        Map<K, V> result = new TreeMap<>(StringPrettyComparator::doCompare);
        result.putAll(map);

        return result;
    }


    public static void main(String[] args) {
        org.apache.jena.graph.Graph g = GraphFactory.createDefaultGraph();
        g.add(new Triple(Vars.s, Vars.p, Vars.o));
        RDFDataMgr.write(System.out, g, RDFFormat.NTRIPLES);
//        String[][] cases = {
//            { "Prefix : <http://ex.org/> Select * { ?a ?b ?c }",
//              "Prefix : <http://ex.org/> Select * { ?x ?y ?z }", },
//            { "Prefix : <http://ex.org/> Select * { ?d a ?f ; ?g ?h }",
//              "Prefix : <http://ex.org/> Select * { ?x a ?o ; ?y ?z }" },
//            { "Prefix : <http://ex.org/> Select * { ?i a :Bakery ; :locatedIn :Leipzig }",
//              "Prefix : <http://ex.org/> Select * { ?x a ?o . ?x ?y ?z }" },
//            { "Prefix : <http://ex.org/> Select * { ?x a ?o . ?x ?y ?z . ?z a ?w}" }
//        };

        String[][] cases = {
                { "Prefix : <http://ex.org/> Select * { ?a ?a ?a }",
                  "Prefix : <http://ex.org/> Select * { ?x ?x ?x . ?y ?y ?y }",
                  "Prefix : <http://ex.org/> Select * { ?x ?x ?x . ?y ?y ?y . ?z ?z ?z }" },

                { "Prefix : <http://ex.org/> Select * { ?as ?ap ?ao }",
                  "Prefix : <http://ex.org/> Select * { ?js ?jp ?jo . ?ks ?kp ?ko }",
                  "Prefix : <http://ex.org/> Select * { ?xs ?xp ?xo . ?ys ?yp ?yo . ?zs ?zp ?zo }",
                  "Prefix : <http://ex.org/> Select * { ?sa ?sp ?so . ?ts ?tp ?to .}" },

                // test examples with overlapping variables
                { "Prefix : <http://ex.org/> Select * { ?as ?ap ?ao }",
                  "Prefix : <http://ex.org/> Select * { ?xs ?xp ?xo . ?ys ?yp ?yo }",
                  "Prefix : <http://ex.org/> Select * { ?xs ?xp ?xo . ?ys ?yp ?yo . ?zs ?zp ?zo }" }

            };

        String caseA = cases[1][0];
        String caseB = cases[1][1];
        String caseC = cases[1][3];

        // This does not work with jgrapht due to lack of support for multi edges!!!

        Op aop = Algebra.toQuadForm(Algebra.compile(QueryFactory.create(caseA)));
        Op bop = Algebra.toQuadForm(Algebra.compile(QueryFactory.create(caseB)));
        Op cop = Algebra.toQuadForm(Algebra.compile(QueryFactory.create(caseC)));
        //System.out.println(op);


        aop = SparqlViewMatcherOpImpl.normalizeOp(aop);
        bop = SparqlViewMatcherOpImpl.normalizeOp(bop);
        cop = SparqlViewMatcherOpImpl.normalizeOp(cop);


        //RDFDataMgr.write(System.out, graph, RDFFormat.NTRIPLES);

        Supplier<Supplier<Node>> ssn = () -> { int[] x = {0}; return () -> NodeFactory.createBlankNode("_" + x[0]++); };
        QueryToGraphVisitor av = new ExtendedQueryToGraphVisitor(ssn.get());
        aop.visit(av);
        GraphVar ag = av.getGraph();
        //System.out.println(ag.get);

        QueryToGraphVisitor bv = new ExtendedQueryToGraphVisitor(ssn.get());
        bop.visit(bv);
        GraphVar bg = bv.getGraph();

        QueryToGraphVisitor cv = new ExtendedQueryToGraphVisitor(ssn.get());
        cop.visit(cv);
        GraphVar cg = cv.getGraph();


//        System.out.println("Graph A:");
//        RDFDataMgr.write(System.out, ag.getWrapped(), RDFFormat.NTRIPLES);
//        System.out.println(ag.getVarToNode());
//
//        System.out.println("Graph B:");
//        RDFDataMgr.write(System.out, bg.getWrapped(), RDFFormat.NTRIPLES);
//
        List<Map<Node, Node>> solutions = QueryToJenaGraph.match(HashBiMap.create(), bg, cg).collect(Collectors.toList());
//
//        System.out.println("VarMap entries: " + solutions.size());

        solutions.forEach(varMap -> {
            System.out.println(prettify(varMap));
        });


        GraphIndex<Node> index = GraphIndex.create();
        int xxx = 1;

        if(xxx == 0) {
            // incremental subsumtion
            index.add(ag);
            index.add(bg);
            index.add(cg);
        } else {
            // most generic inserted last
            index.add(bg);
            index.add(cg);
            index.add(ag);
        }

        System.out.println("Performing lookup");
        index.lookup(cg).entries().forEach(e -> System.out.println("Lookup result: " + e.getKey() + ": " + prettify(e.getValue())));


        //SparqlQueryContainmentUtils.match(viewQuery, userQuery, qfpcMatcher)
        org.jgrapht.Graph<?, ?> dg = new PseudoGraphJenaGraph(cg);
        //System.out.println(graph);
        if(false) {
            visualizeGraph(dg);
        }
    }


    public static void visualizeGraph(org.jgrapht.Graph<?, ?> graph) {
        JFrame frame = new JFrame();
        frame.setSize(1000, 800);
        JGraph jgraph = new JGraph(new JGraphModelAdapter(graph));
        jgraph.setScale(1);
        final JGraphLayout hir = new JGraphHierarchicalLayout();
        // final JGraphLayout hir = new JGraphSelfOrganizingOrganicLayout();

        final JGraphFacade graphFacade = new JGraphFacade(jgraph);
        hir.run(graphFacade);
        final Map nestedMap = graphFacade.createNestedMap(true, true);
        jgraph.getGraphLayoutCache().edit(nestedMap);

        frame.getContentPane().add(jgraph);
        frame.setVisible(true);
    }

}
//// Do this state space search thingy: update the state, track the changes, compute and restore
//// This means: track which keys will be added, add them, and later remove them again
//boolean isCompatible = MapUtils.isCompatible(iso, baseIso);
//if(!isCompatible) {
//  writer.println("Not compatible with current mapping");
//  writer.incIndent();
//  writer.println("baseIso: " + baseIso);
//  writer.println("iso: " + iso);
//  writer.decIndent();
//  throw new RuntimeException("This should never happen - unless either there is a bug or even worse there is a conecptual issues");
//  //return;
//}
//
