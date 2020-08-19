package org.aksw.jena_sparql_api.shape.algebra.op;

import org.apache.jena.sparql.path.Path;

public class OpFocus
    extends Op1
{
    protected Path path;

    public OpFocus(Op subOp, Path path) {
        super(subOp);
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public <T> T accept(OpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

}
