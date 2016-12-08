package org.aksw.jena_sparql_api.shape.syntax;

import org.apache.jena.sparql.expr.Expr;

public class ElementFilter
    extends Element0
{
    protected Expr expr;

    public ElementFilter(Expr expr) {
        super();
        this.expr = expr;
    }

    public Expr getExpr() {
        return expr;
    }

    @Override
    public <T> T accept(ElementVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

}
