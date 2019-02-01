package org.aksw.jena_sparql_api_sparql_path2.playground;

import java.util.Map;

import org.apache.jena.graph.Node;

public interface JoinSummaryService {
    Map<Node, Map<Node, Number>> fetch(Iterable<Node> predicates, boolean reverse);
//    join(Predicate
}
