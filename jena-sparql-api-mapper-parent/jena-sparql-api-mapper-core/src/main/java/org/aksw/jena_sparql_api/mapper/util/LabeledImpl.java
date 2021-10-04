package org.aksw.jena_sparql_api.mapper.util;

public class LabeledImpl<T>
	implements Labeled<T>
{
	protected T object;
	protected String label;
	
	public LabeledImpl(T object, String label) {
		super();
		this.object = object;
		this.label = label;
	}

	@Override
	public T getObject() {
		return object;
	}

	@Override
	public String getLabel() {
		return label;
	}
	
	@Override
	public String toString() {
		String result = getLabel();
		return result;
	}
}
