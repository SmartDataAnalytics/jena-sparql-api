package org.aksw.jena_sparql_api.mapper.jpa.metamodel;

import java.util.Set;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

public interface AttributeSupport<X> {
	Set<Attribute<X, ?>> getAttributes();
	<Y> SingularAttribute<X, Y> getSingularAttribute(String name, Class<Y> type);
	Set<SingularAttribute<X, ?>> getSingularAttributes();
	<E> CollectionAttribute<X, E> getCollection(String name, Class<E> elementType);
	<E> SetAttribute<X, E> getSet(String name, Class<E> elementType);
	<E> ListAttribute<X, E> getList(String name, Class<E> elementType);
	<K, V> MapAttribute<X, K, V> getMap(String name, Class<K> keyType, Class<V> valueType);
	Set<PluralAttribute<X, ?, ?>> getPluralAttributes();
	Attribute<X, ?> getAttribute(String name);
	SingularAttribute<X, ?> getSingularAttribute(String name);
	CollectionAttribute<X, ?> getCollection(String name);
	SetAttribute<X, ?> getSet(String name);
	ListAttribute<X, ?> getList(String name);
	MapAttribute<X, ?, ?> getMap(String name);
}
