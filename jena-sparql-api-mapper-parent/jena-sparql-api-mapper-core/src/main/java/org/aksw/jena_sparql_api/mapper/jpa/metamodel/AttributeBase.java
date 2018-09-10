package org.aksw.jena_sparql_api.mapper.jpa.metamodel;

import java.lang.reflect.Member;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;

public abstract class AttributeBase<X, Y>
	implements Attribute<X, Y>
{
	protected ManagedType<X> declaringType;
	protected String name;
	protected Class<Y> javaType;
	protected Member javaMember;

	public AttributeBase(ManagedType<X> declaringType, String name, Class<Y> javaType, Member javaMember) {
		super();
		this.declaringType = declaringType;
		this.name = name;
		this.javaType = javaType;
	}

	@Override
	public ManagedType<X> getDeclaringType() {
		return declaringType;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Class<Y> getJavaType() {
		return javaType;
	}

	@Override
	public Member getJavaMember() {
		return javaMember;
	}
}
