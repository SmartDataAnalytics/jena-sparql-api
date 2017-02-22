package org.aksw.jena_sparql_api.mapper.jpa.criteria;

import java.util.Collection;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;

import org.aksw.jena_sparql_api.mapper.jpa.criteria.expr.ExpressionVisitor;

public abstract class ExpressionBase<T>
	extends SelectionImpl<T>
    implements Expression<T>
{
//    protected String aliasName;
//
//    public ExpressionBase(Expr expr) {
//    	super(null, null);
//        this.expr = expr;
//    }
//
//    public Expr getJenaExpr() {
//        return expr;
//    }

    public ExpressionBase(Class<T> javaClass) {
		super(javaClass);
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
    public Class<? extends T> getJavaType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAlias() {
        // TODO Auto-generated method stub
        return null;
    }

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

    // TODO We could use Jena's polymorphic principle
    @SuppressWarnings("unchecked")
	@Override
    public <X> Expression<X> as(Class<X> type) {
//    	@SuppressWarnings("unchecked")
//		Expression<X> result = type.isAssignableFrom(this.getClass()))
//			? (Expression<X>)this
//			: null;

		return (Expression<X>)this;
    }
    
    public abstract <X> X accept(ExpressionVisitor<X> visitor);
}
