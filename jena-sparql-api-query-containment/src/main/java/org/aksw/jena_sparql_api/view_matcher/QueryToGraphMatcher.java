package org.aksw.jena_sparql_api.view_matcher;

import java.util.Map;
import java.util.stream.Stream;

import org.aksw.commons.graph.index.jena.transform.QueryToGraph;
import org.aksw.commons.jena.jgrapht.LabeledEdge;
import org.aksw.commons.jena.jgrapht.LabeledEdgeImpl;
import org.aksw.jena_sparql_api.algebra.utils.AlgebraUtils;
import org.aksw.jena_sparql_api.algebra.utils.QuadFilterPatternCanonical;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleGraph;

public class QueryToGraphMatcher {


    public static void toGraph(Graph<Node, LabeledEdge<Node, Node>> graph, QuadFilterPatternCanonical qfpc) {
        QueryToGraph.quadsToGraph(graph, qfpc.getQuads());
        QueryToGraph.equalExprsToGraph(graph, qfpc.getFilterDnf());
    }

    public static void toGraph(Graph<Node, LabeledEdge<Node, Node>> graph, Query query) {
        QuadFilterPatternCanonical qfpc = AlgebraUtils.fromQuery(query);
        toGraph(graph, qfpc);
    }

    public static Graph<Node, LabeledEdge<Node, Node>> toGraph(QuadFilterPatternCanonical qfpc) {
        //EdgeFactory <Node, LabeledEdge<Node, Node>> edgeFactory = (v, e) -> new LabeledEdgeImpl<>(v, e, null);
//        Graph<Node, LabeledEdge<Node, Node>> graph = new SimpleGraph<>((v, e) -> new LabeledEdgeImpl<>(v, e, null));

    	Graph<Node, LabeledEdge<Node, Node>> graph = new SimpleGraph<>(null, () -> new LabeledEdgeImpl<>(), false);// (v, e) -> new LabeledEdgeImpl<>(v, e, null));

        toGraph(graph, qfpc);

        return graph;
    }
    public static Stream<Map<Var, Var>> match(QuadFilterPatternCanonical view, QuadFilterPatternCanonical user) {
        Graph<Node, LabeledEdge<Node, Node>> a = toGraph(view);
        Graph<Node, LabeledEdge<Node, Node>> b = toGraph(user);

        Stream<Map<Var, Var>> result = QueryToGraph.match(a, b);
        return result;
    }

    /**
     * Convenience method for testing.
     * Only works for queries whose element is a BGP + filters.
     *
     * @param view
     * @param user
     * @return
     */
    public static boolean tryMatch(Query view, Query user) {
//        Graph<Node, LabeledEdge<Node, Node>> a = new SimpleGraph<>((v, e) -> new LabeledEdgeImpl<>(v, e, null));
//        Graph<Node, LabeledEdge<Node, Node>> b = new SimpleGraph<>((v, e) -> new LabeledEdgeImpl<>(v, e, null));

        Graph<Node, LabeledEdge<Node, Node>> a = new SimpleGraph<>(null, () -> new LabeledEdgeImpl<>(), false);
        Graph<Node, LabeledEdge<Node, Node>> b = new SimpleGraph<>(null, () -> new LabeledEdgeImpl<>(), false);

        toGraph(a, view);
        toGraph(b, user);


//		visualizeGraph(a);
//		visualizeGraph(b);
//		try(Scanner s = new Scanner(System.in)) { s.nextLine(); }



        Stream<Map<Var, Var>> tmp = QueryToGraph.match(a, b);
        tmp = tmp.peek(x -> System.out.println("Solution: " + x));
        boolean result = tmp.count() > 0;
        return result;
    }

}
