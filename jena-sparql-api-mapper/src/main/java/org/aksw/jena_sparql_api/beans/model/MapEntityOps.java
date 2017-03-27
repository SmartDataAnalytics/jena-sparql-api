package org.aksw.jena_sparql_api.beans.model;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Wrapper to treat a Java object as a map with a set of predefined keys.
 * Note: As this breaks the map contract (similar to some cache implementations),
 * it may be better introduce a separate interface which is similar to map yet different.
 * 
 * @author raven
 *
 */
public class MapEntityOps
    extends AbstractMap<String, Object>
{
    protected EntityOps entityOps;
    protected Object entity;

    public MapEntityOps(EntityOps entityOps, Object entity) {
        this.entityOps = entityOps;
        this.entity = entity;
    }
    
    public EntityOps getEntityOps() {
        return entityOps;
    }

    public Object getEntity() {
        return entity;
    }

    @Override
    public Object get(Object key) {
        Object result = null;
        if(key instanceof String) {
            PropertyOps propertyOps = entityOps.getProperty((String)key);
            if(propertyOps != null) {
                result = propertyOps.getValue(entity);
            }
        }

        return result;
    }
    
    @Override
    public Object put(String key, Object value) {
        PropertyOps propertyOps = entityOps.getProperty(key);
        if(propertyOps == null) {
            throw new RuntimeException("Cannot set value of non-existent property " + key);
        }
        
        propertyOps.setValue(entity, value);
        return value;
    }
    
    @Override
    public Set<Entry<String, Object>> entrySet() {
        Set<Entry<String, Object>> result = new HashSet<>();
        for(PropertyOps propertyOps : entityOps.getProperties()) {
            String name = propertyOps.getName();
            Object value = propertyOps.getValue(entity);
            Entry<String, Object> entry = new SimpleEntry<>(name, value);
            result.add(entry);
        }
        return result;
    }
}
