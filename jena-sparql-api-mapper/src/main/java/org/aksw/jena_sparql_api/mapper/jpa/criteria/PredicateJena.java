package org.aksw.jena_sparql_api.mapper.jpa.criteria;

import java.util.Collection;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;

import com.hp.hpl.jena.sparql.expr.Expr;

public class PredicateJena
    extends ExpressionJena<Boolean>
    implements Predicate
{

    public PredicateJena(Expr expr) {
        super(expr);
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
        // TODO Auto-generated method stub
        return null;
    }

}
