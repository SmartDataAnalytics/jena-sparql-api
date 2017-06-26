package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;

/**
 * TODO This interface needs to be extended to
 * cover all possible expressions of the JPA
 *
 * @author raven
 *
 * @param <T>
 */
public interface ExpressionVisitor<T>
    extends PathVisitor<T>
{
    //T visit(VPath<?> e);

    T visit(LogicalNotExpression e);

    T visit(LogicalAndExpression e);
    T visit(EqualsExpression e);
    T visit(ValueExpression<?> e);

    T visit(GreatestExpression<?> e);
    T visit(AvgExpression e);

    T visit(GreaterThanExpression e);
    T visit(GreaterThanOrEqualToExpression e);
}
