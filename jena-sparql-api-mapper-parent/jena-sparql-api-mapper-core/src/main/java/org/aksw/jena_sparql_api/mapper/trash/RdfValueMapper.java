package org.aksw.jena_sparql_api.mapper.trash;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;

public interface RdfValueMapper {
    void writeValue(Object value, Node subject, Node predicate, Graph outputGraph);
    Object readValue(Graph graph, Node subject, Node predicate);
}
