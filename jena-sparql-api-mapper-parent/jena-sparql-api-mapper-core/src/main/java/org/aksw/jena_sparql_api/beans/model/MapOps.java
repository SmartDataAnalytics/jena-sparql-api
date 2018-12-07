package org.aksw.jena_sparql_api.beans.model;

import java.util.Map.Entry;
import java.util.Set;

public interface MapOps {
    Class<?> getAssociatedClass();    
    
    // Return the top-level class for keys / values 
    Class<?> getKeyClass();
    Class<?> getValueClass();
    
//    <A> A findAnnotation(Class<A> annotationClass);
    
    void put(Object entity, Object key, Object value);
    Object get(Object entity, Object key);

    boolean containsKey(Object entity, Object value);
    void remove(Object entity, Object key);
    void clear(Object entity);
    
    Set<? extends Object> keySet(Object entity);
    Set<? extends Entry<? extends Object, ? extends Object>> entrySet(Object entity);
    int size(Object entity);
}
