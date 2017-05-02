package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;

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
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.SetJoin;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

public class FromImpl<Z, X>
    extends PathImpl<X>
    implements VFrom<Z, X>
{
    // TODO Would be great if consistency checks with the metamodel to raise errors early could be delegated to a lambda
    // in order to improve re-use
    // protected Function<> resolver;

    public FromImpl(Path<?> parentPath, String attrName, Class<X> valueType) {
        super(parentPath, attrName, valueType);
    }

//    @Override
//    public <Y> Path<Y> get(SingularAttribute<? super X, Y> attribute) {
//        String attrName = attribute.getName();
//        Path<Y> result = get(attrName);
//        return result;
//    }

    @Override
    public Set<Fetch<X, ?>> getFetches() {

        throw new UnsupportedOperationException();
    }

    @Override
    public <Y> Fetch<X, Y> fetch(SingularAttribute<? super X, Y> attribute) {

        throw new UnsupportedOperationException();
    }

    @Override
    public <Y> Fetch<X, Y> fetch(SingularAttribute<? super X, Y> attribute, JoinType jt) {

        throw new UnsupportedOperationException();
    }

    @Override
    public <Y> Fetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute) {

        throw new UnsupportedOperationException();
    }

    @Override
    public <Y> Fetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute, JoinType jt) {

        throw new UnsupportedOperationException();
    }

    @Override
    public <X, Y> Fetch<X, Y> fetch(String attributeName) {

        throw new UnsupportedOperationException();
    }

    @Override
    public <X, Y> Fetch<X, Y> fetch(String attributeName, JoinType jt) {

        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Join<X, ?>> getJoins() {

        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCorrelated() {

        return false;
    }

    @Override
    public From<Z, X> getCorrelationParent() {

        throw new UnsupportedOperationException();
    }

    @Override
    public <Y> Join<X, Y> join(SingularAttribute<? super X, Y> attribute) {

        throw new UnsupportedOperationException();
    }

    @Override
    public <Y> Join<X, Y> join(SingularAttribute<? super X, Y> attribute, JoinType jt) {

        throw new UnsupportedOperationException();
    }

    @Override
    public <Y> CollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection) {

        throw new UnsupportedOperationException();
    }

    @Override
    public <Y> SetJoin<X, Y> join(SetAttribute<? super X, Y> set) {

        throw new UnsupportedOperationException();
    }

    @Override
    public <Y> ListJoin<X, Y> join(ListAttribute<? super X, Y> list) {

        throw new UnsupportedOperationException();
    }

    @Override
    public <K, V> MapJoin<X, K, V> join(MapAttribute<? super X, K, V> map) {

        throw new UnsupportedOperationException();
    }

    @Override
    public <Y> CollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection, JoinType jt) {

        throw new UnsupportedOperationException();
    }

    @Override
    public <Y> SetJoin<X, Y> join(SetAttribute<? super X, Y> set, JoinType jt) {

        throw new UnsupportedOperationException();
    }

    @Override
    public <Y> ListJoin<X, Y> join(ListAttribute<? super X, Y> list, JoinType jt) {

        throw new UnsupportedOperationException();
    }

    @Override
    public <K, V> MapJoin<X, K, V> join(MapAttribute<? super X, K, V> map, JoinType jt) {

        throw new UnsupportedOperationException();
    }

    @Override
    public <X, Y> Join<X, Y> join(String attributeName) {

        throw new UnsupportedOperationException();
    }

    @Override
    public <X, Y> CollectionJoin<X, Y> joinCollection(String attributeName) {

        throw new UnsupportedOperationException();
    }

    @Override
    public <X, Y> SetJoin<X, Y> joinSet(String attributeName) {

        throw new UnsupportedOperationException();
    }

    @Override
    public <X, Y> ListJoin<X, Y> joinList(String attributeName) {

        throw new UnsupportedOperationException();
    }

    @Override
    public <X, K, V> MapJoin<X, K, V> joinMap(String attributeName) {

        throw new UnsupportedOperationException();
    }

    @Override
    public <X, Y> Join<X, Y> join(String attributeName, JoinType jt) {

        throw new UnsupportedOperationException();
    }

    @Override
    public <X, Y> CollectionJoin<X, Y> joinCollection(String attributeName, JoinType jt) {

        throw new UnsupportedOperationException();
    }

    @Override
    public <X, Y> SetJoin<X, Y> joinSet(String attributeName, JoinType jt) {

        throw new UnsupportedOperationException();
    }

    @Override
    public <X, Y> ListJoin<X, Y> joinList(String attributeName, JoinType jt) {

        throw new UnsupportedOperationException();
    }

    @Override
    public <X, K, V> MapJoin<X, K, V> joinMap(String attributeName, JoinType jt) {

        throw new UnsupportedOperationException();
    }
}
