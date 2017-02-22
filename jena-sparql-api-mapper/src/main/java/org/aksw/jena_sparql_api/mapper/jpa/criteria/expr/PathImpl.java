package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;

import javax.persistence.criteria.Path;

public class PathImpl<X>
	extends org.aksw.jena_sparql_api.mapper.jpa.criteria.PathImpl<X>
	implements VExpression<X>
{

	public PathImpl(Path<?> parentPath, String attrName, Class<X> valueType) {
		super(parentPath, attrName, valueType);
	}

	@SuppressWarnings("unchecked")
	public <T> VExpression<T> as(Class<T> cls) {
		return (VExpression<T>)this; 
	}
}
