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
	implements From<Z, X>
{
	//protected Function<> resolver;
	
	@Override
	public Bindable<X> getModel() {		
		return null;
	}

	@Override
	public Path<?> getParentPath() {		
		throw new UnsupportedOperationException();
	}

	@Override
	public <Y> Path<Y> get(SingularAttribute<? super X, Y> attribute) {
		String attrName = attribute.getName();
		Path<Y> result = get(attrName);
		return result;
	}

	@Override
	public <E, C extends Collection<E>> Expression<C> get(PluralAttribute<X, C, E> collection) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <K, V, M extends Map<K, V>> Expression<M> get(MapAttribute<X, K, V> map) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Expression<Class<? extends X>> type() {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public <Y> Path<Y> get(String attributeName) {
		Path<Y> result = new PathImpl<>(null, attributeName, null);
		return result;
	}

	@Override
	public Predicate isNull() {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public Predicate isNotNull() {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public Predicate in(Object... values) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public Predicate in(Expression<?>... values) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public Predicate in(Collection<?> values) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public Predicate in(Expression<Collection<?>> values) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public <X> Expression<X> as(Class<X> type) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public Selection<X> alias(String name) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isCompoundSelection() {
		
		return false;
	}

	@Override
	public List<Selection<?>> getCompoundSelectionItems() {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public Class<? extends X> getJavaType() {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public String getAlias() {
		
		throw new UnsupportedOperationException();
	}

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
