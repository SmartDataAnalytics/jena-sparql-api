package org.aksw.jena_sparql_api.mapper.jpa.criteria;

import javax.persistence.TupleElement;

public class TupleElementBase<X>
	implements TupleElement<X>
{
	protected Class<? extends X> javaType;
	protected String alias;
	
	public TupleElementBase(Class<? extends X> javaType, String alias) {
		super();
		this.javaType = javaType;
		this.alias = alias;
	}

	@Override
	public Class<? extends X> getJavaType() {
		return javaType;
	}

	@Override
	public String getAlias() {
		return alias;
	}
}
