package org.aksw.jena_sparql_api_sparql_path2;

import java.util.function.Function;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.Path;
import org.jgrapht.VertexFactory;
import org.jgrapht.graph.DefaultDirectedGraph;

public class PathExecutionUtils {

    public static Nfa<Integer, LabeledEdge<Integer, Path>> compileToNfa(Path path) {
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
        Nfa<Integer, LabeledEdge<Integer, Path>> result = nfaCompiler.complete();

        return result;
    }

    public static void executePath(Path path, Node startNode, Node targetNode, QueryExecutionFactory qef, Function<RdfPath, Boolean> pathCallback) {

        Nfa<Integer, LabeledEdge<Integer, Path>> nfa = compileToNfa(path);


        System.out.println("NFA");
        System.out.println(nfa);
        for(LabeledEdge<Integer, Path> edge : nfa.getGraph().edgeSet()) {
            System.out.println(edge);
        }

//        PartialNfa<Integer, Path> peek = nfaCompiler.peek();

        //QueryExecutionFactory qef = FluentQueryExecutionFactory.http("http://dbpedia.org/sparql", "http://dbpedia.org").config().selectOnly().end().create();

        NfaExecution<Integer> exec = new NfaExecution<Integer>(nfa, qef);
        exec.add(startNode);
        while(exec.advance()) {
            System.out.println("advancing...");
        }

    }
}
