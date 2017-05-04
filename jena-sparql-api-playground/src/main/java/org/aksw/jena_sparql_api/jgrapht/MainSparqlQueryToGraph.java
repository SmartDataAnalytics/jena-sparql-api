package org.aksw.jena_sparql_api.jgrapht;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.JFrame;

import org.aksw.jena_sparql_api.concept_cache.op.OpExtConjunctiveQuery;
import org.aksw.jena_sparql_api.jgrapht.transform.GraphIsoMap;
import org.aksw.jena_sparql_api.jgrapht.transform.GraphIsoMapImpl;
import org.aksw.jena_sparql_api.jgrapht.transform.GraphVar;
import org.aksw.jena_sparql_api.jgrapht.transform.GraphVarImpl;
import org.aksw.jena_sparql_api.jgrapht.transform.QueryToGraphVisitor;
import org.aksw.jena_sparql_api.jgrapht.transform.QueryToJenaGraph;
import org.aksw.jena_sparql_api.jgrapht.wrapper.PseudoGraphJenaGraph;
import org.aksw.jena_sparql_api.views.index.SparqlViewMatcherOpImpl;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.compose.Difference;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.jgraph.JGraph;
import org.jgrapht.ext.JGraphModelAdapter;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
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


class GraphIndexNode {

    // The graph at this node
    protected GraphIsoMap graphIso;

    protected List<GraphIndexNode> children;


    GraphIndexNode(GraphIsoMap graphIso) {
        this.graphIso = graphIso;
        children = new ArrayList<>();
    }


    /**
     * During the insert procedure, the insert graph is never renamed, because we want to figure out
     * how to remap existing nodes such they become a subgraph of the insertGraph.
     *
     * @param graph
     */
    void add(Graph insertGraph, BiMap<Node, Node> currentIso) {
        // The insert graph must be larger than the node Graph

        System.out.println("Insert attempt");
        RDFDataMgr.write(System.out, insertGraph, RDFFormat.NTRIPLES);
        System.out.println("under: " + currentIso);

        boolean isSubsumed = false;
        //children.forEach(child -> {
        for(GraphIndexNode child : children) {

            //BiMap<Node, Node> isoMap = child.graphIso.getOutToIn();
            //Graph candGraph = new GraphIsoMapImpl(graph, isoMap);
            GraphIsoMap viewGraph = child.graphIso;

            System.out.println("Comparing with:");
            RDFDataMgr.write(System.out, viewGraph, RDFFormat.NTRIPLES);
            System.out.println("under: " + currentIso);

            // For every found isomorphism, check all children whether they are also isomorphic.
            //
            QueryToJenaGraph.match(viewGraph, insertGraph).forEach(iso -> {
                // iso: how to rename nodes of the view graph so it matches with the insert graph
                Graph g = new GraphIsoMapImpl(viewGraph, iso);

                Difference diff = new Difference(g, viewGraph); // left side should be the smaller graph for performance

                System.out.println("Diff");
                RDFDataMgr.write(System.out, diff, RDFFormat.NTRIPLES);
                // now create the diff between the insert graph and mapped child graph
            });

        }

        // If the insertGraph was not subsumed, add it as a new child
        if(!isSubsumed) {
            GraphIsoMap gim = new GraphIsoMapImpl(insertGraph, currentIso);
            children.add(new GraphIndexNode(gim));
        }
    }

    //void find(Graph graph)
}


class GraphIndex
{
    GraphIndexNode root = new GraphIndexNode(new GraphIsoMapImpl(new GraphVarImpl(), HashBiMap.create()));



    /**
     *
     *
     * @param graph
     */
    void add(Graph graph) {
        root.add(graph, HashBiMap.create());

    }

}



public class MainSparqlQueryToGraph {



    public static void main(String[] args) {
//        org.apache.jena.graph.Graph g = GraphFactory.createDefaultGraph();
//        g.add(new Triple(Vars.s, Vars.p, Vars.o));
//        RDFDataMgr.write(System.out, g, RDFFormat.NTRIPLES);
        String[][] cases = {
            { "Prefix : <http://ex.org/> Select * { ?s ?p ?o }",
              "Prefix : <http://ex.org/> Select * { ?x ?y ?z }" },
            { "Prefix : <http://ex.org/> Select * { ?s a ?t ; ?p ?o }",
              "Prefix : <http://ex.org/> Select * { ?x a ?o ; ?y ?z }" },
            { "Prefix : <http://ex.org/> Select * { ?s a :Bakery ; :locatedIn :Leipzig }",
              "Prefix : <http://ex.org/> Select * { ?x a ?o . ?x ?y ?z }" }
        };


        int i = 0;
        String caseA = cases[i][0];
        String caseB = cases[i][1];

        Op aop = Algebra.toQuadForm(Algebra.compile(QueryFactory.create(caseA)));
        Op bop = Algebra.toQuadForm(Algebra.compile(QueryFactory.create(caseB)));
        //System.out.println(op);


        aop = SparqlViewMatcherOpImpl.normalizeOp(aop);
        bop = SparqlViewMatcherOpImpl.normalizeOp(bop);


        //RDFDataMgr.write(System.out, graph, RDFFormat.NTRIPLES);

        Supplier<Supplier<Node>> ssn = () -> { int[] x = {0}; return () -> NodeFactory.createBlankNode("_" + x[0]++); };
        QueryToGraphVisitor av = new ExtendedQueryToGraphVisitor(ssn.get());
        aop.visit(av);
        GraphVar ag = av.getGraph();
        //System.out.println(ag.get);

        QueryToGraphVisitor bv = new ExtendedQueryToGraphVisitor(ssn.get());
        bop.visit(bv);
        GraphVar bg = bv.getGraph();


        System.out.println("Graph A:");
        RDFDataMgr.write(System.out, ag.getWrapped(), RDFFormat.NTRIPLES);
        System.out.println(ag.getVarToNode());
        System.out.println();
        System.out.println("Graph B:");
        RDFDataMgr.write(System.out, bg.getWrapped(), RDFFormat.NTRIPLES);

        List<Map<Node, Node>> solutions = QueryToJenaGraph.match(bg, ag).collect(Collectors.toList());

        System.out.println("VarMap entries: " + solutions.size());
        solutions.forEach(varMap -> System.out.print(varMap));


        GraphIndex index = new GraphIndex();
        index.add(ag);
        index.add(bg);


        //SparqlQueryContainmentUtils.match(viewQuery, userQuery, qfpcMatcher)
        org.jgrapht.Graph<?, ?> dg = new PseudoGraphJenaGraph(ag);
        //System.out.println(graph);
        visualizeGraph(dg);
    }


    public static void visualizeGraph(org.jgrapht.Graph<?, ?> graph) {
        JFrame frame = new JFrame();
        frame.setSize(1000, 800);
        JGraph jgraph = new JGraph(new JGraphModelAdapter(graph));
        jgraph.setScale(2);
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
