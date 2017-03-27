package org.aksw.jena_sparql_api.mapper.jpa.criteria;

import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.EntityType;

import org.apache.jena.sparql.expr.Expr;

public class CriteriaQueryJena<T>
    implements CriteriaQuery<T>
{
    protected List<Expr> where;

    @Override
    public CriteriaQuery<T> where(Expression<Boolean> restriction) {
        if(restriction instanceof ExpressionJena) {
            ExpressionJena<Boolean> e = (ExpressionJena<Boolean>)restriction;
            //this.where = e.getJenaExpr();
        } else {
            throw new RuntimeException("Invalid expression type");
        }


        return this;
    }

    @Override
    public Class<T> getResultType() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public CriteriaQuery<T> where(Predicate... restrictions) {
        throw new UnsupportedOperationException();
    }


    @Override
    public <X> Root<X> from(Class<X> entityClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <X> Root<X> from(EntityType<X> entity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public <U> Subquery<U> subquery(Class<U> type) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Root<?>> getRoots() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Selection<T> getSelection() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Predicate getRestriction() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Expression<?>> getGroupList() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Predicate getGroupRestriction() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDistinct() {
        // TODO Auto-generated method stub
        return false;
    }



    @Override
    public CriteriaQuery<T> select(Selection<? extends T> selection) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public CriteriaQuery<T> multiselect(Selection<?>... selections) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public CriteriaQuery<T> multiselect(List<Selection<?>> selectionList) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public CriteriaQuery<T> groupBy(Expression<?>... grouping) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public CriteriaQuery<T> groupBy(List<Expression<?>> grouping) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public CriteriaQuery<T> having(Expression<Boolean> restriction) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public CriteriaQuery<T> having(Predicate... restrictions) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public CriteriaQuery<T> orderBy(Order... o) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public CriteriaQuery<T> orderBy(List<Order> o) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public CriteriaQuery<T> distinct(boolean distinct) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Order> getOrderList() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<ParameterExpression<?>> getParameters() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

}
