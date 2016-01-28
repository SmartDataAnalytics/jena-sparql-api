package org.aksw.jena_sparql_api_sparql_path2;

import java.util.List;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.path.Path;


public interface SparqlPathFinder {
    List<SparqlPath> find(Node startNode, Node endNode, int k, Path path, QueryExecutionFactory qef);
}
