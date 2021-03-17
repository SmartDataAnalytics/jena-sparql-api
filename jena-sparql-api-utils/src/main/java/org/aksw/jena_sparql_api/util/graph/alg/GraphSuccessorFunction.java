package org.aksw.jena_sparql_api.util.graph.alg;

import java.util.stream.Stream;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import com.github.jsonldjava.shaded.com.google.common.collect.Streams;

public interface GraphSuccessorFunction {
	Stream<Node> apply(Graph graph, Node node);
	
	/**
	 * Create a function that yields for a given Graph and Node a Stream of successors
	 * based on a predicate and direction.
	 * 
	 * @param predicate
	 * @param isForward
	 * @return
	 */
	public static GraphSuccessorFunction create(Node predicate, boolean isForward) {
		return isForward
				? (graph, node) -> Streams.stream(graph.find(node, predicate, Node.ANY)
						.mapWith(Triple::getObject))
				: (graph, node) -> Streams.stream(graph.find(Node.ANY, predicate, node)
						.mapWith(Triple::getSubject));
	}

}
