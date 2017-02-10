package org.aksw.jena_sparql_api.mapper.impl.type;

import java.util.Map;

/**
 * An object that contains information about how to resolve the property values
 * of an entity
 * 
 * 
 * @author raven
 *
 */
public class EntityFragment {
	protected Map<String, EntityPlaceholderInfo> propertyToInfo;
	
	public Map<String, EntityPlaceholderInfo> getPropertyInfo() {
		return propertyToInfo;
	}
}
