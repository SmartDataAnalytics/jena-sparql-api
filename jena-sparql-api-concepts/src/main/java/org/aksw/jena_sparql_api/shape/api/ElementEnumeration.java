package org.aksw.jena_sparql_api.shape.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Node;

public class ElementEnumeration
    extends Element0
{
    protected List<Node> values;

    public ElementEnumeration() {
        super();
        this.values = new ArrayList<Node>();
    }

    public List<Node> getValues() {
        return values;
    }

}
