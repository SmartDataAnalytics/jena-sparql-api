package org.aksw.jena_sparql_api_sparql_path2;

import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathParser;
import org.jgrapht.VertexFactory;
import org.jgrapht.graph.DefaultDirectedGraph;

import com.google.common.collect.Multimap;


public class MainSparqlPath2 {


    public static void main(String[] args) {
//        Prologue prologue = new Prologue();

        Path path = PathParser.parse("((<p>/<z>)|<x>)*", PrefixMapping.Extended);
        //Path path = PathParser.parse("!(<p>|(<p>|<p>))", PrefixMapping.Extended);

        /*
         * Some ugly set up of graph related stuff
         */
        EdgeFactoryLabeledEdge<Integer, Path> edgeFactory = new EdgeFactoryLabeledEdge<Integer, Path>();
        EdgeLabelAccessorImpl<Integer, LabeledEdge<Integer, Path>, Path> edgeLabelAccessor = new EdgeLabelAccessorImpl<Integer, LabeledEdge<Integer, Path>, Path>();
        DefaultDirectedGraph<Integer, LabeledEdge<Integer, Path>> graph = new DefaultDirectedGraph<Integer, LabeledEdge<Integer, Path>>(edgeFactory);
        VertexFactory<Integer> vertexFactory = new VertexFactoryInteger(graph);

        PathVisitorNfaCompilerImpl<Integer> nfaCompiler = new PathVisitorNfaCompilerImpl<Integer>(graph, vertexFactory, edgeLabelAccessor);


        /*
         * The actual nfa conversion step
         */
        path.visit(nfaCompiler);
        Nfa<Integer, LabeledEdge<Integer, Path>> nfa = nfaCompiler.complete();


        System.out.println("NFA");
        System.out.println(nfa);
        for(LabeledEdge<Integer, Path> edge : nfa.getGraph().edgeSet()) {
            System.out.println(edge);
        }

//        PartialNfa<Integer, Path> peek = nfaCompiler.peek();

        NfaExecution<Integer> exec = new NfaExecution<Integer>(nfa);
        Multimap<Integer, LabeledEdge<Integer, Path>> transitions = exec.getTransitions();

        System.out.println("Is final?" + exec.isFinalState(3));

        System.out.println("Transitions: " + transitions);


        //System.out.println(peek);
//        System.out.println("Start state: " + peek.getStartVertex());
//        System.out.println("Final transitions: " + peek.getLooseEnds());

        System.out.println(path + " " + path.getClass());
    }



}
