package org.aksw.jena_sparql_api.mapper.impl.type;

import java.util.IdentityHashMap;
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
	protected Object rootEntity;
	protected Map<Object, Map<String, EntityPlaceholderInfo>> propertyInfos;
	
	public EntityFragment(Object rootEntity) {
		this.rootEntity = rootEntity; 
		this.propertyInfos = new IdentityHashMap<>();
	}
	
	public Map<Object, Map<String, EntityPlaceholderInfo>> getPropertyInfos() {
		return propertyInfos;
	}

	public Object getRootEntity() {
		return rootEntity;
	}
}
