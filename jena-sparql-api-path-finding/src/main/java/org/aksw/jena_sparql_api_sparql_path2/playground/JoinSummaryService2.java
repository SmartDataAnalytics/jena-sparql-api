package org.aksw.jena_sparql_api_sparql_path2.playground;

import java.util.Map;

import org.apache.jena.graph.Node;

/**
 * Alternative approach of the join summary service that only yields the set of
 * successor properties
 *
 * @author raven
 *
 */
public interface JoinSummaryService2 {
    Map<Node, Number> fetchPredicates(Iterable<Node> predicates, boolean reverse);
}
