package org.aksw.jena_sparql_api.shape.syntax;

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

    @Override
    public <T> T accept(ElementVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

}
