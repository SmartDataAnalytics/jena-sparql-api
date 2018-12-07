package org.aksw.jena_sparql_api.mapper.jpa.metamodel;

import javax.persistence.metamodel.Type;

public abstract class TypeBase<X> implements Type<X> {
	protected Class<X> cls;

	// @Override
	// public PersistenceType getPersistenceType() {
	//
	// }
	public TypeBase(Class<X> cls) {
		super();
		this.cls = cls;
	}

	@Override
	public Class<X> getJavaType() {
		return cls;
	}
}
