package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;


public class AvgExpression
    extends UnaryOperatorExpression<Double>
{
    public AvgExpression(VExpression<? extends Number> operand) {
        super(Double.class, operand);
    }

    @Override
    public <X> X accept(ExpressionVisitor<X> visitor) {
        X result = visitor.visit(this);
        return result;
    }

}
