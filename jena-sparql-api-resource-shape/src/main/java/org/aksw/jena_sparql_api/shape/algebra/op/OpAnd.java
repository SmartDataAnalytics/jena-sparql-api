package org.aksw.jena_sparql_api.shape.algebra.op;

public class OpAnd
    extends Op2
{

    public OpAnd(Op left, Op right) {
        super(left, right);
    }

    @Override
    public <T> T accept(OpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

}
