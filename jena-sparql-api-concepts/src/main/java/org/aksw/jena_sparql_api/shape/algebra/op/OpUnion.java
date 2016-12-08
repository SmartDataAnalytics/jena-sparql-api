package org.aksw.jena_sparql_api.shape.algebra.op;

public class OpUnion
    extends Op2
{

    public OpUnion(Op left, Op right) {
        super(left, right);
    }

    @Override
    public <T> T accept(OpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

}
