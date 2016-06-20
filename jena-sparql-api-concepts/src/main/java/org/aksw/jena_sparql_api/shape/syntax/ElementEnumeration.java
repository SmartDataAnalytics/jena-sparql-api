package org.aksw.jena_sparql_api.shape.syntax;

import java.util.Arrays;
import java.util.List;

import org.apache.jena.graph.Node;

public class ElementEnumeration
    extends Element0
{
    protected List<Node> values;

    public ElementEnumeration(Node ...nodes) {
        super();
        this.values = Arrays.asList(nodes);//new ArrayList<Node>();
    }

    public ElementEnumeration(List<Node> nodes) {
        super();
        this.values = nodes; //Arrays.asList(nodes);//new ArrayList<Node>();
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
