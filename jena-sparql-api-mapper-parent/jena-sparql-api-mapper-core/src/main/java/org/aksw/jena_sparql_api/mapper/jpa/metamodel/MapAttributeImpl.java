package org.aksw.jena_sparql_api.mapper.jpa.metamodel;

import java.lang.reflect.Member;
import java.util.Map;

import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.Type;

public class MapAttributeImpl<X, K, V>
	extends PluralAttributeBase<X, Map<K, V>, V>
	implements MapAttribute<X, K, V>
{

	public MapAttributeImpl(ManagedType<X> declaringType, String name, Class<Map<K, V>> javaType, Member javaMember) {
		super(declaringType, name, javaType, javaMember);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Class<K> getKeyJavaType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type<K> getKeyType() {
		// TODO Auto-generated method stub
		return null;
	}

}
