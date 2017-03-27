package org.aksw.jena_sparql_api.mapper.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ValueHolderImpl
	implements ValueHolder
{
	protected Supplier<?> getter;
	protected Consumer<Object> setter;
	
	public ValueHolderImpl(Supplier<?> getter, Consumer<Object> setter) {
		super();
		this.getter = getter;
		this.setter = setter;
	}

	@Override
	public Object getValue() {
		Object result = getter.get();
		return result;
	}

	@Override
	public void setValue(Object value) {
		setter.accept(value);
	}
}
