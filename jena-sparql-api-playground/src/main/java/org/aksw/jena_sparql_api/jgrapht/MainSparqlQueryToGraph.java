package org.aksw.jena_sparql_api.jgrapht;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.JFrame;

import org.aksw.combinatorics.solvers.ProblemNeighborhoodAware;
import org.aksw.commons.util.strings.StringPrettyComparator;
import org.aksw.jena_sparql_api.iso.index.SubGraphIsomorphismIndex;
import org.aksw.jena_sparql_api.jgrapht.transform.GraphVar;
import org.aksw.jena_sparql_api.jgrapht.transform.QueryToGraphVisitor;
import org.aksw.jena_sparql_api.jgrapht.transform.QueryToJenaGraph;
import org.aksw.jena_sparql_api.jgrapht.wrapper.PseudoGraphJenaGraph;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.jena_sparql_api.views.index.SparqlViewMatcherOpImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.GraphFactory;
import org.jgraph.JGraph;
import org.jgrapht.ext.JGraphModelAdapter;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.JGraphLayout;
import com.jgraph.layout.hierarchical.JGraphHierarchicalLayout;


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
                  "Prefix : <http://ex.org/> Select * { ?ss ?sp ?so . ?ts ?tp ?to .}" ,
                  "Prefix : <http://ex.org/> Select * { ?xs ?xp ?xo . ?ys ?yp ?yo . ?zs ?zp ?zo }"},

                // test examples with overlapping variables
                { "Prefix : <http://ex.org/> Select * { ?as ?ap ?ao }",
                  "Prefix : <http://ex.org/> Select * { ?xs ?xp ?xo . ?ys ?yp ?yo }",
                  "Prefix : <http://ex.org/> Select * { ?xs ?xp ?xo . ?ys ?yp ?yo . ?zs ?zp ?zo }" }

            };

        String caseA = cases[1][0];
        String caseB = cases[1][1];
        String caseC = cases[1][2];
        String caseD = cases[1][3];

        // This does not work with jgrapht due to lack of support for multi edges!!!

        Op aop = Algebra.toQuadForm(Algebra.compile(QueryFactory.create(caseA)));
        Op bop = Algebra.toQuadForm(Algebra.compile(QueryFactory.create(caseB)));
        Op cop = Algebra.toQuadForm(Algebra.compile(QueryFactory.create(caseC)));
        Op dop = Algebra.toQuadForm(Algebra.compile(QueryFactory.create(caseD)));
        //System.out.println(op);


        aop = SparqlViewMatcherOpImpl.normalizeOp(aop);
        bop = SparqlViewMatcherOpImpl.normalizeOp(bop);
        cop = SparqlViewMatcherOpImpl.normalizeOp(cop);
        dop = SparqlViewMatcherOpImpl.normalizeOp(dop);


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

        QueryToGraphVisitor dv = new ExtendedQueryToGraphVisitor(ssn.get());
        dop.visit(dv);
        GraphVar dg = dv.getGraph();

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


        SubGraphIsomorphismIndex<Node> index = SubGraphIsomorphismIndex.create();
        int xxx = 1;

        if(xxx == 0) {
            // incremental subsumtion
            index.add(ag);
            index.add(bg);
            index.add(cg);
        } else {
            // most generic inserted last
            index.add(dg);
            //index.add(cg);
            index.add(bg);
            index.add(ag);
        }

//        System.out.println("Performing lookup");
//        index.lookupFlat(cg).entries().forEach(e -> System.out.println("Lookup result: " + e.getKey() + ": " + prettify(e.getValue())));

        System.out.println("Index tree: ");
        index.printTree();

        Map<Node, ProblemNeighborhoodAware<BiMap<Var, Var>, Var>> map = index.lookupStream(cg, false);

        map.forEach((k, p) -> {
            System.out.println("Solutions for : " + k);
            //System.out.println("Estimated cost: " + p.getEstimatedCost());
            p.generateSolutions().forEach(s -> {
                System.out.println("  " + s);
            });
            System.out.println("done");
        });



        //SparqlQueryContainmentUtils.match(viewQuery, userQuery, qfpcMatcher)
        org.jgrapht.Graph<?, ?> xg = new PseudoGraphJenaGraph(dg);
        //System.out.println(graph);
        if(false) {
            visualizeGraph(xg);
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
