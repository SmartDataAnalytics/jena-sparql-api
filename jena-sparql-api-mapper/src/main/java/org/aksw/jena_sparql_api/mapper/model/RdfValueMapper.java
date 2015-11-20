package org.aksw.jena_sparql_api.mapper.model;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;

public interface RdfValueMapper {
    void writeValue(Object value, Node subject, Node predicate, Graph outputGraph);
    Object readValue(Graph graph, Node subject, Node predicate);
}
