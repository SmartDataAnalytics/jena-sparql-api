package org.aksw.jena_sparql_api.mapper.jpa.criteria;

import javax.persistence.criteria.Order;

import org.aksw.jena_sparql_api.mapper.jpa.criteria.expr.VExpression;

public class OrderImpl
    implements Order
{
    protected VExpression<?> expression;
    protected boolean isAscending;

    public OrderImpl(boolean isAscending, VExpression<?> expression) {
        super();
        this.isAscending = isAscending;
        this.expression = expression;
    }

    @Override
    public Order reverse() {
        return new OrderImpl(!isAscending, expression);
    }

    @Override
    public boolean isAscending() {
        return isAscending;
    }

    @Override
    public VExpression<?> getExpression() {
        return expression;
    }
}
