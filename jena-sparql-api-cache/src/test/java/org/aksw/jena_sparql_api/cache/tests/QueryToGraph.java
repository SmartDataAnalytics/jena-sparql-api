package org.aksw.jena_sparql_api.cache.tests;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.jena_sparql_api.concept_cache.core.SparqlCacheUtils;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.jgrapht.LabeledEdge;
import org.aksw.jena_sparql_api.jgrapht.LabeledEdgeImpl;
import org.aksw.jena_sparql_api.utils.DnfUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphMapping;
import org.jgrapht.alg.isomorphism.VF2SubgraphIsomorphismInspector;
import org.jgrapht.graph.SimpleDirectedGraph;

public class QueryToGraph {
	public static void addEdge(DirectedGraph<Node, LabeledEdge<Node, Node>> graph, Node edgeLabel, Node source, Node target) {
		graph.addVertex(source);
		graph.addVertex(target);
		graph.addEdge(source, target, new LabeledEdgeImpl<>(source, target, edgeLabel));
	}

	public static void addQuad(DirectedGraph<Node, LabeledEdge<Node, Node>> graph, Quad quad) {
		// Allocate a fresh node for the quad
		Node quadNode = NodeFactory.createBlankNode();
		addEdge(graph, Vars.s, quad.getSubject(), quadNode);
		addEdge(graph, Vars.p, quadNode, quad.getPredicate());
		addEdge(graph, Vars.o, quadNode, quad.getObject());
		addEdge(graph, Vars.g, quadNode, quad.getGraph());
	}


	public static void quadsToGraph(DirectedGraph<Node, LabeledEdge<Node, Node>> graph, Collection<Quad> quads) {
		//graph = new DirectedPseudograph<>(LabeledEdgeImpl.class);
		for(Quad quad : quads) {
			addQuad(graph, quad);
		}
	}

	// Filters: Extract all equality filters
	public static void equalExprsToGraph(DirectedGraph<Node, LabeledEdge<Node, Node>> graph, Collection<? extends Collection<? extends Expr>> dnf) {
		Set<Map<Var, NodeValue>> maps = DnfUtils.extractConstantConstraints(dnf);

		for(Map<Var, NodeValue> map : maps) {
			// Create a blank node for each clause
			Node orNode = NodeFactory.createBlankNode();
			graph.addVertex(orNode);

			for(Entry<Var, NodeValue> e : map.entrySet()) {
				Var v = e.getKey();
				Node c = e.getValue().getNode();

				graph.addVertex(v);
				graph.addVertex(c);

				graph.addEdge(orNode, v, new LabeledEdgeImpl<Node, Node>(orNode, v, Vars.x));
				graph.addEdge(c, orNode, new LabeledEdgeImpl<Node, Node>(c, orNode, Vars.y));
			}
		}
	}


	public static void toGraph(DirectedGraph<Node, LabeledEdge<Node, Node>> graph, QuadFilterPatternCanonical qfpc) {
		quadsToGraph(graph, qfpc.getQuads());
		equalExprsToGraph(graph, qfpc.getFilterDnf());
	}

	public static void queryToGraph(DirectedGraph<Node, LabeledEdge<Node, Node>> graph, Query query) {
		QuadFilterPatternCanonical qfpc = SparqlCacheUtils.fromQuery(query);
		toGraph(graph, qfpc);
	}

	public static void tryMatch(Query view, Query user) {
		DirectedGraph<Node, LabeledEdge<Node, Node>> a = new SimpleDirectedGraph<>(LabeledEdgeImpl.class);
		DirectedGraph<Node, LabeledEdge<Node, Node>> b = new SimpleDirectedGraph<>(LabeledEdgeImpl.class);

		queryToGraph(a, view);
		queryToGraph(b, user);

		//System.out.println("EDGES:");
		//a.edgeSet().forEach(System.out::println);
		//System.out.println("done with edges");

		Comparator<Node> nodeCmp = (x, y) -> {
			int  r = (x.isVariable() && y.isVariable()) || (x.isBlank() && y.isBlank()) ? 0 : x.toString().compareTo(y.toString());
			//System.out.println("" + x + " - " + y + ": " + r);
			return r;
		};
		Comparator<LabeledEdge<Node, Node>> edgeCmp = (x, y) -> x.getLabel().toString().compareTo(y.getLabel().toString());
//		Comparator<//LabeledEd>
		VF2SubgraphIsomorphismInspector<Node, LabeledEdge<Node, Node>> inspector = new VF2SubgraphIsomorphismInspector<>(a, b, nodeCmp, edgeCmp, true);
		Iterator<GraphMapping<Node, LabeledEdge<Node, Node>>> it = inspector.getMappings();
		while(it.hasNext()) {
			GraphMapping<Node, LabeledEdge<Node, Node>> x = it.next();
			Map<Var, Var> varMap = new HashMap<>();
			for(Node node : a.vertexSet()) {
				if(node.isVariable()) {
					Var s = (Var)node;
					Node fff = x.getVertexCorrespondence(s, false);
					if(fff.isVariable()) {
						varMap.put(s, (Var)fff);
					}
				}
			}
			System.out.println(varMap);

			System.out.println(x.getClass());
		}

	}


}

