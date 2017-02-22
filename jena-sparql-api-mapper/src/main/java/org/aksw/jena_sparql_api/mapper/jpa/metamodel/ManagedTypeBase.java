package org.aksw.jena_sparql_api.mapper.jpa.metamodel;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

/**
 * Base implementation of ManagedType which delegates calls to
 * AttributeSupport, which allows for significantly simpler, more
 * modular, and less redundant implementations.
 * 
 * @author raven
 *
 * @param <X>
 */
public abstract class ManagedTypeBase<X>
	extends TypeBase<X>
	implements ManagedType<X>
{
	protected AttributeSupport<? super X> attributes;
	protected AttributeSupport<X> declaredAttributes;

	public ManagedTypeBase(Class<X> cls, AttributeSupport<X> attributes, AttributeSupport<X> declaredAttributes) {
		super(cls);
		this.attributes = attributes;
		this.declaredAttributes = declaredAttributes;
	}

	@Override
	public Set<Attribute<? super X, ?>> getAttributes() {
		//return Collections.unmodifiableSet(attributes.getAttributes());		
		//return attributes.getAttributes();
		return new HashSet<>(attributes.getAttributes());
		//return attributes.getAttributes().stream().collect(Collectors.toSet());
	}
	
	@Override
	public Attribute<? super X, ?> getAttribute(String name) {
		return attributes.getAttribute(name);
	}

	@Override
	public Set<SingularAttribute<? super X, ?>> getSingularAttributes() {
		return new HashSet<>(attributes.getSingularAttributes());
	}

	@Override
	public SingularAttribute<? super X, ?> getSingularAttribute(String name) {
		return attributes.getSingularAttribute(name);
	}

	@Override
	public <Y> SingularAttribute<? super X, Y> getSingularAttribute(String name, Class<Y> type) {
		return attributes.getSingularAttribute(name, type);
	}

	@Override
	public Set<PluralAttribute<? super X, ?, ?>> getPluralAttributes() {
		return new HashSet<>(attributes.getPluralAttributes());
	}

	@Override
	public CollectionAttribute<? super X, ?> getCollection(String name) {
		return attributes.getCollection(name);
	}

	@Override
	public <E> CollectionAttribute<? super X, E> getCollection(String name, Class<E> elementType) {
		return attributes.getCollection(name, elementType);
	}

	@Override
	public SetAttribute<? super X, ?> getSet(String name) {
		return attributes.getSet(name);
	}

	@Override
	public <E> SetAttribute<? super X, E> getSet(String name, Class<E> elementType) {
		return attributes.getSet(name, elementType);
	}

	@Override
	public ListAttribute<? super X, ?> getList(String name) {
		return attributes.getList(name);
	}

	@Override
	public <E> ListAttribute<? super X, E> getList(String name, Class<E> elementType) {
		return attributes.getList(name, elementType);
	}

	@Override
	public MapAttribute<? super X, ?, ?> getMap(String name) {
		return attributes.getMap(name);
	}

	@Override
	public <K, V> MapAttribute<? super X, K, V> getMap(String name, Class<K> keyType, Class<V> valueType) {
		return attributes.getMap(name, keyType, valueType);
	}
	
	
	
	
	@Override
	public Set<Attribute<X, ?>> getDeclaredAttributes() {
		return declaredAttributes.getAttributes();
	}
	
	@Override
	public Attribute<X, ?> getDeclaredAttribute(String name) {
		return declaredAttributes.getAttribute(name);
	}

	@Override
	public Set<SingularAttribute<X, ?>> getDeclaredSingularAttributes() {
		return declaredAttributes.getSingularAttributes();
	}

	@Override
	public SingularAttribute<X, ?> getDeclaredSingularAttribute(String name) {
		return declaredAttributes.getSingularAttribute(name);
	}

	@Override
	public <Y> SingularAttribute<X, Y> getDeclaredSingularAttribute(String name, Class<Y> type) {
		return declaredAttributes.getSingularAttribute(name, type);
	}

	@Override
	public Set<PluralAttribute<X, ?, ?>> getDeclaredPluralAttributes() {
		return declaredAttributes.getPluralAttributes();
	}

	@Override
	public CollectionAttribute<X, ?> getDeclaredCollection(String name) {
		return declaredAttributes.getCollection(name);
	}

	@Override
	public <E> CollectionAttribute<X, E> getDeclaredCollection(String name, Class<E> elementType) {
		return declaredAttributes.getCollection(name, elementType);
	}

	@Override
	public SetAttribute<X, ?> getDeclaredSet(String name) {
		return declaredAttributes.getSet(name);
	}

	@Override
	public <E> SetAttribute<X, E> getDeclaredSet(String name, Class<E> elementType) {
		return declaredAttributes.getSet(name, elementType);
	}

	@Override
	public ListAttribute<X, ?> getDeclaredList(String name) {
		return declaredAttributes.getList(name);
	}

	@Override
	public <E> ListAttribute<X, E> getDeclaredList(String name, Class<E> elementType) {
		return declaredAttributes.getList(name, elementType);
	}

	@Override
	public MapAttribute<X, ?, ?> getDeclaredMap(String name) {
		return declaredAttributes.getMap(name);
	}

	@Override
	public <K, V> MapAttribute<X, K, V> getDeclaredMap(String name, Class<K> keyType, Class<V> valueType) {
		return declaredAttributes.getMap(name, keyType, valueType);
	}

}
