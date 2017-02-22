package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;

import javax.persistence.criteria.Path;

public class PathImpl<X>
	extends org.aksw.jena_sparql_api.mapper.jpa.criteria.PathImpl<X>
	implements Expression<X>
{

	public PathImpl(Path<?> parentPath, String attrName, Class<X> valueType) {
		super(parentPath, attrName, valueType);
	}

	@SuppressWarnings("unchecked")
	public <T> Expression<T> as(Class<T> cls) {
		return (Expression<T>)this; 
	}
}
