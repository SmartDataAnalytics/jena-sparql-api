package org.aksw.jena_sparql_api_sparql_path2;

import java.util.Iterator;
import java.util.concurrent.Future;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.Path;

/**
 * Interface for finding k shortest paths between a given start and end nodes
 * Either of the nodes may be null (or Node.ANY - but not both) in which case all accepting paths will be returned.
 *
 * Implementing classes are expected to be bound to the appropriate dataset.
 *
 *
 * @author raven
 *
 */
public interface SparqlKShortestPathFinder {
    Iterator<NestedPath<Node, Node>> findPaths(Node start, Node end, Path pathExpr, Long k);
}
