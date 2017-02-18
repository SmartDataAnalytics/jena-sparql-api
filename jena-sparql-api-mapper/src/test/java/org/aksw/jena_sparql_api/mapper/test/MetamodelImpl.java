package org.aksw.jena_sparql_api.mapper.test;

import java.util.Set;

import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

import org.hibernate.ejb.metamodel.EntityTypeImpl;

public class MetamodelImpl
	implements Metamodel
{

	@Override
	public <X> EntityType<X> entity(Class<X> cls) {
		return new EntityTypeImpl<>(cls, null, cls.getSimpleName(), false, false);
	}

	@Override
	public <X> ManagedType<X> managedType(Class<X> cls) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X> EmbeddableType<X> embeddable(Class<X> cls) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<ManagedType<?>> getManagedTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<EntityType<?>> getEntities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<EmbeddableType<?>> getEmbeddables() {
		// TODO Auto-generated method stub
		return null;
	}

}
