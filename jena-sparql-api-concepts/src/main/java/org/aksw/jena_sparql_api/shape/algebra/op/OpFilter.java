package org.aksw.jena_sparql_api.shape.algebra.op;

import org.apache.jena.sparql.expr.Expr;

public class OpFilter
    extends Op1
{
    protected Expr expr;

    public OpFilter(Op subOp, Expr expr) {
        super(subOp);
        this.expr = expr;
    }

    public Expr getExpr() {
        return expr;
    }

    @Override
    public <T> T accept(OpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

}
