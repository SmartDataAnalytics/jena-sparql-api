package org.aksw.jena_sparql_api.mapper.jpa.metamodel;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;


public class MetamodelImpl
	implements Metamodel
{
	protected Map<Class<?>, ManagedType<?>> managedTypes;
	protected Map<Class<?>, EntityType<?>> entities;
	protected Map<Class<?>, EmbeddableType<?>> embeddables;
	
	@Override
	public Set<ManagedType<?>> getManagedTypes() {
		Set<ManagedType<?>> result = new HashSet<>(managedTypes.values());
		return result;
	}

	@Override
	public Set<EntityType<?>> getEntities() {
		Set<EntityType<?>> result = new HashSet<>(entities.values());
		return result;
	}

	@Override
	public Set<EmbeddableType<?>> getEmbeddables() {
		Set<EmbeddableType<?>> result = new HashSet<>(embeddables.values());
		return result;
	}

	@Override
	public <X> EntityType<X> entity(Class<X> cls) {
		@SuppressWarnings("unchecked")
		EntityType<X> result = (EntityType<X>)entities.get(cls);
		return result;
	}

	@Override
	public <X> ManagedType<X> managedType(Class<X> cls) {
		@SuppressWarnings("unchecked")
		ManagedType<X> result = (EntityType<X>)entities.get(cls);
		return result;
	}

	@Override
	public <X> EmbeddableType<X> embeddable(Class<X> cls) {
		@SuppressWarnings("unchecked")
		EmbeddableType<X> result = (EmbeddableType<X>)entities.get(cls);
		return result;
	}

}
