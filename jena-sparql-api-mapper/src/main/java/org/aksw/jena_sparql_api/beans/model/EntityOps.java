package org.aksw.jena_sparql_api.beans.model;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public interface EntityOps {
    // Entity ops may but need to be derived from a java class
    Class<?> getAssociatedClass();
    
    /**
     * Operations may be associated with an entity.
     * For example, an entity may support map operations (put, get, etc...)
     * While such an entity may e.g. have a (read-only) size property, the get / put operations
     * would be part of separate operations class associate with this entityOps instance.
     *   
     */
    <T> T getOps(Class<T> opsClass);
    
    <A> A findAnnotation(Class<A> annotationClass);
    
    
    // If false, newInstance() should not be called.
    boolean isInstantiable();
    
    Object newInstance();
    Collection<? extends PropertyOps> getProperties();
    
    PropertyOps getProperty(String name);
    
    default Set<String> getPropertyNames() {
        Set<String> result = getProperties().stream().map(p -> p.getName()).collect(Collectors.toSet());
        return result;
    }
    
    public static void copy(EntityOps sourceOps, EntityOps targetOps, Object fromEntity, Object toEntity) {
        for(PropertyOps toOps : targetOps.getProperties()) {
            String name = toOps.getName();
            PropertyOps fromOps = sourceOps.getProperty(name);
            
            Object value = fromOps.getValue(fromEntity);
            toOps.setValue(toEntity, value);
        }
    }
}
