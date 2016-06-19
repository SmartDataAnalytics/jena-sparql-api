package org.aksw.jena_sparql_api.shape.algebra.op;

import org.apache.jena.graph.Node;

public class OpType
    extends Op0
{
    protected Node type;

    public OpType(Node type) {
        super();
        this.type = type;
    }

    public Node getType() {
        return type;
    }

    @Override
    public <T> T accept(OpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

}
