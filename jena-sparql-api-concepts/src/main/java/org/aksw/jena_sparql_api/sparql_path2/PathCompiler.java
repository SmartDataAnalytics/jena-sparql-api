package org.aksw.jena_sparql_api.sparql_path2;

import java.util.function.Supplier;

import org.aksw.commons.jena.jgrapht.LabeledEdge;
import org.aksw.jena_sparql_api.util.sparql.syntax.path.PathVisitorRewriteInvert;
import org.aksw.jena_sparql_api.util.sparql.syntax.path.PathVisitorTopDown;
import org.apache.jena.sparql.path.Path;
import org.jgrapht.graph.DefaultDirectedGraph;

public class PathCompiler {

    public static Nfa<Integer, LabeledEdge<Integer, PredicateClass>> compileToNfa(Path path) {
        //Path path = PathParser.parse("!(<p>|(<p>|<p>))", PrefixMapping.Extended);

        path = PathVisitorTopDown.apply(path, new PathVisitorRewriteInvert());

        /*
         * Some ugly set up of graph related stuff
         */
        EdgeFactoryLabeledEdge<Integer, PredicateClass> edgeFactory = new EdgeFactoryLabeledEdge<Integer, PredicateClass>();
        EdgeLabelAccessor<LabeledEdge<Integer, PredicateClass>, PredicateClass> edgeLabelAccessor = new EdgeLabelAccessorImpl<Integer, LabeledEdge<Integer, PredicateClass>, PredicateClass>();
        DefaultDirectedGraph<Integer, LabeledEdge<Integer, PredicateClass>> graph = new DefaultDirectedGraph<Integer, LabeledEdge<Integer, PredicateClass>>(edgeFactory);
        Supplier<Integer> vertexFactory = new VertexFactoryInteger(graph);

        PathVisitorNfaCompilerImpl<Integer, LabeledEdge<Integer, PredicateClass>, PredicateClass> nfaCompiler = new PathVisitorNfaCompilerImpl<Integer, LabeledEdge<Integer, PredicateClass>, PredicateClass>(graph, vertexFactory, edgeLabelAccessor, x -> PathVisitorPredicateClass.transform(x));

        /*
         * The actual nfa conversion step
         */
        path.visit(nfaCompiler);
        Nfa<Integer, LabeledEdge<Integer, PredicateClass>> result = nfaCompiler.complete();

        return result;
    }

}
