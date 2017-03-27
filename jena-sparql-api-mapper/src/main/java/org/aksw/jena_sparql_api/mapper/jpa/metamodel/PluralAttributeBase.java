package org.aksw.jena_sparql_api.mapper.jpa.metamodel;

import java.lang.reflect.Member;

import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.Type;

public class PluralAttributeBase<X, C, E>
	extends AttributeBase<X, C>
	implements PluralAttribute<X, C, E>
{

	public PluralAttributeBase(ManagedType<X> declaringType, String name, Class<C> javaType, Member javaMember) {
		super(declaringType, name, javaType, javaMember);
		// TODO Auto-generated constructor stub
	}

	@Override
	public javax.persistence.metamodel.Attribute.PersistentAttributeType getPersistentAttributeType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAssociation() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCollection() {
		return true;
	}

	@Override
	public javax.persistence.metamodel.Bindable.BindableType getBindableType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<E> getBindableJavaType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public javax.persistence.metamodel.PluralAttribute.CollectionType getCollectionType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type<E> getElementType() {
		// TODO Auto-generated method stub
		return null;
	}

}
