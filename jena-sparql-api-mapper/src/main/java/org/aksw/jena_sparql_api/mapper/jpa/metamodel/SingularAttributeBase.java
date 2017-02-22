package org.aksw.jena_sparql_api.mapper.jpa.metamodel;

import java.lang.reflect.Member;
import java.util.function.Function;

import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

public abstract class SingularAttributeBase<X, T>
	extends AttributeBase<X, T>
	implements SingularAttribute<X, T>
{
	protected Function<Class<?>, Type<?>> typeResolver;

	public SingularAttributeBase(
			Function<Class<?>, Type<?>> typeResolver,
			ManagedType<X> declaringType,
			String name,
			Class<T> javaType,
			Member javaMember) {
		super(declaringType, name, javaType, javaMember);
	}

	@Override
	public boolean isAssociation() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCollection() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public javax.persistence.metamodel.Bindable.BindableType getBindableType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<T> getBindableJavaType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isId() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isVersion() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOptional() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Type<T> getType() {
		@SuppressWarnings("unchecked")
		Type<T> result = (Type<T>)typeResolver.apply(javaType);
		return result;
	}

}
