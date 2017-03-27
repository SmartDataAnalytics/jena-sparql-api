package org.aksw.jena_sparql_api.shape.syntax;

import org.apache.jena.graph.Node;

public class ElementValue {
    protected Node value;

    public ElementValue(Node value) {
        super();
        this.value = value;
    }

    public Node getValue() {
        return value;
    }
}
