package org.aksw.jena_sparql_api.shape.algebra.op;

import java.util.List;

import org.apache.jena.graph.Node;

public class OpEnumeration
    extends Op0
{
    protected List<Node> nodes;

    public OpEnumeration(List<Node> nodes) {
        super();
        this.nodes = nodes;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    @Override
    public <T> T accept(OpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

}
