package org.aksw.jena_sparql_api.mapper.jpa.criteria;

import java.util.Collection;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;

import org.aksw.jena_sparql_api.mapper.jpa.criteria.expr.LogicalNotPredicate;

public abstract class PredicateBase
    extends ExpressionBase<Boolean>
    implements Predicate
{

	public PredicateBase() {
		super(Boolean.class);
	}

//    public PredicateBase(Expression<Boolean> expression) {
//        super(expression, null);
//    }

    @Override
    public Predicate isNull() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Predicate isNotNull() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Predicate in(Object... values) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Predicate in(Expression<?>... values) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Predicate in(Collection<?> values) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Predicate in(Expression<Collection<?>> values) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X> Expression<X> as(Class<X> type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Selection<Boolean> alias(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isCompoundSelection() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<Selection<?>> getCompoundSelectionItems() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<? extends Boolean> getJavaType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAlias() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BooleanOperator getOperator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isNegated() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<Expression<Boolean>> getExpressions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Predicate not() {
    	return new LogicalNotPredicate(this);
    }

//	@Override
//	public <X> X accept(ExpressionVisitor<X> visitor) {
//		// TODO Auto-generated method stub
//		return null;
//	}

}
