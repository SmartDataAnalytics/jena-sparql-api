package org.aksw.jena_sparql_api.mapper.context;

import java.util.Map;

import org.springframework.beans.PropertyValue;

public class BeanState {
	/**
	 * The bean which eventually should become populated
	 */
	protected Object bean;

	protected Map<String, PropertyValue> value;

	protected boolean isPopulated;

	public void setPopulated(boolean isPopulated) {
		this.isPopulated = isPopulated;
	}

	public boolean isPopulated() {
		return isPopulated;
	}
}
