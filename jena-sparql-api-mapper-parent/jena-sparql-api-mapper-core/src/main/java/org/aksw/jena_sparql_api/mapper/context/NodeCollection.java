package org.aksw.jena_sparql_api.mapper.context;

import java.util.List;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.graph.Node;

/**
 * An entity representing a set of nodes.
 * 
 * @author raven
 *
 */
public interface NodeCollection {
    
    
    List<Node> resolve(QueryExecutionFactory qef);
}
