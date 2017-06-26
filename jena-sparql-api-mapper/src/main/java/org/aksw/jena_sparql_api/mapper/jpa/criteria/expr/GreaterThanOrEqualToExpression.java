package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;

public class GreaterThanOrEqualToExpression
    extends BinaryOperatorExpression<Boolean>
{
    public GreaterThanOrEqualToExpression(VExpression<?> a, VExpression<?> b) {
        super(Boolean.class, a, b);
    }

    @Override
    public <X> X accept(ExpressionVisitor<X> visitor) {
        X result = visitor.visit(this);
        return result;
    }

}
