package org.aksw.jena_sparql_api.mapper.model;

import org.apache.jena.graph.Node;

public interface NodeMapper {
    Class<?> getValueClass();
    Object toJava(Node node);
    Node toNode(Object value);
}
