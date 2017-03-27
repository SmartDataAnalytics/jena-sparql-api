package org.aksw.jena_sparql_api.mapper.jpa.criteria;

import java.util.List;

import javax.persistence.criteria.Selection;

public class SelectionImpl<X>
	extends TupleElementBase<X>	
	implements Selection<X>
{
	//protected Expression<X> expression;
	
	public SelectionImpl(Class<? extends X> javaType) {
		super(javaType, null);
		//super(expression.getJavaType(), alias);
		//this.expression = expression;
	}

	@Override
	public Selection<X> alias(String name) {
//		Selection<X> result = new SelectionImpl<X>(expression, name);
//		return result;
		this.alias = name;
		return this;
	}

	@Override
	public boolean isCompoundSelection() {
		return false;
	}

	@Override
	public List<Selection<?>> getCompoundSelectionItems() {
		return null;
	}
}
