package org.aksw.jena_sparql_api.mapper.jpa.metamodel;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

/**
 * Simple AttributeSupport implementation built upon a single Map 
 * 
 * @author raven
 *
 */
public class AttributeSuportBase<X>
	implements AttributeSupport<X>
{
	protected Map<String, Attribute<X, ?>> nameToAttribute;

	@Override
	public Set<Attribute<X, ?>> getAttributes() {
		Set<Attribute<X, ?>> result = new HashSet<>(nameToAttribute.values());
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <Y> SingularAttribute<X, Y> getSingularAttribute(String name, Class<Y> type) {
		return (SingularAttribute<X, Y>)nameToAttribute.get(name);
	}

	@Override
	public Set<SingularAttribute<X, ?>> getSingularAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> CollectionAttribute<X, E> getCollection(String name, Class<E> elementType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> SetAttribute<X, E> getSet(String name, Class<E> elementType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> ListAttribute<X, E> getList(String name, Class<E> elementType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <K, V> MapAttribute<X, K, V> getMap(String name, Class<K> keyType, Class<V> valueType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<PluralAttribute<X, ?, ?>> getPluralAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Attribute<X, ?> getAttribute(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SingularAttribute<X, ?> getSingularAttribute(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CollectionAttribute<X, ?> getCollection(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SetAttribute<X, ?> getSet(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListAttribute<X, ?> getList(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MapAttribute<X, ?, ?> getMap(String name) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
