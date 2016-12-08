package org.aksw.jena_sparql_api.shape.algebra.op;

public class OpTop
    extends Op0
{
    @Override
    public <T> T accept(OpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
