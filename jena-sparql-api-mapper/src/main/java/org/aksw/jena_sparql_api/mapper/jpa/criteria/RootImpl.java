package org.aksw.jena_sparql_api.mapper.jpa.criteria;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.SetJoin;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

public class RootImpl<X>
    implements Root<X>
{

    @Override
    public Set<Join<X, ?>> getJoins() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isCorrelated() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public From<X, X> getCorrelationParent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <Y> Join<X, Y> join(SingularAttribute<? super X, Y> attribute) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <Y> Join<X, Y> join(SingularAttribute<? super X, Y> attribute,
            JoinType jt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <Y> CollectionJoin<X, Y> join(
            CollectionAttribute<? super X, Y> collection) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <Y> SetJoin<X, Y> join(SetAttribute<? super X, Y> set) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <Y> ListJoin<X, Y> join(ListAttribute<? super X, Y> list) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <K, V> MapJoin<X, K, V> join(MapAttribute<? super X, K, V> map) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <Y> CollectionJoin<X, Y> join(
            CollectionAttribute<? super X, Y> collection, JoinType jt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <Y> SetJoin<X, Y> join(SetAttribute<? super X, Y> set, JoinType jt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <Y> ListJoin<X, Y> join(ListAttribute<? super X, Y> list,
            JoinType jt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <K, V> MapJoin<X, K, V> join(MapAttribute<? super X, K, V> map,
            JoinType jt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X, Y> Join<X, Y> join(String attributeName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X, Y> CollectionJoin<X, Y> joinCollection(String attributeName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X, Y> SetJoin<X, Y> joinSet(String attributeName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X, Y> ListJoin<X, Y> joinList(String attributeName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X, K, V> MapJoin<X, K, V> joinMap(String attributeName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X, Y> Join<X, Y> join(String attributeName, JoinType jt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X, Y> CollectionJoin<X, Y> joinCollection(String attributeName,
            JoinType jt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X, Y> SetJoin<X, Y> joinSet(String attributeName, JoinType jt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X, Y> ListJoin<X, Y> joinList(String attributeName, JoinType jt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X, K, V> MapJoin<X, K, V> joinMap(String attributeName,
            JoinType jt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Path<?> getParentPath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <Y> Path<Y> get(SingularAttribute<? super X, Y> attribute) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <E, C extends Collection<E>> Expression<C> get(
            PluralAttribute<X, C, E> collection) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <K, V, M extends Map<K, V>> Expression<M> get(
            MapAttribute<X, K, V> map) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Expression<Class<? extends X>> type() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <Y> Path<Y> get(String attributeName) {
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

    @Override
    public <X> Expression<X> as(Class<X> type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Selection<X> alias(String name) {
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
    public Class<? extends X> getJavaType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAlias() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Fetch<X, ?>> getFetches() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <Y> Fetch<X, Y> fetch(SingularAttribute<? super X, Y> attribute) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <Y> Fetch<X, Y> fetch(SingularAttribute<? super X, Y> attribute,
            JoinType jt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <Y> Fetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <Y> Fetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute,
            JoinType jt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X, Y> Fetch<X, Y> fetch(String attributeName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X, Y> Fetch<X, Y> fetch(String attributeName, JoinType jt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EntityType<X> getModel() {
        // TODO Auto-generated method stub
        return null;
    }

}
