package org.aksw.jena_sparql_api.beans.model;

import java.util.Map.Entry;
import java.util.Set;

public abstract class MapOpsBase<T, K, V>
    implements MapOps
{
    
    // TODO Use the class information to validate arguments
    protected Class<T> entityClass;
    protected Class<K> keyClass;
    protected Class<V> valueClass;
    
    public MapOpsBase(Class<?> entityClass, Class<K> keyClass, Class<V> valueClass) {
        this.keyClass = keyClass;
        this.valueClass = valueClass;
    }

    public Class<?> getAssociatedClass() {
        return entityClass;
    }
    
    public Class<K> getKeyClass() {
        return keyClass;
    }

    public Class<V> getValueClass() {
        return valueClass;
    }


    @SuppressWarnings("unchecked")
    @Override
    public void put(Object entity, Object key, Object value) {        
        $put((T)entity, (K)key, (V)value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean containsKey(Object entity, Object key) {
        return $containsKey((T)entity, key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object get(Object entity, Object key) {
        return $get((T)entity, key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void remove(Object entity, Object key) {
        $remove((T)entity, key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void clear(Object entity) {
        $clear((T)entity);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<? extends Object> keySet(Object entity) {
        return $keySet((T)entity);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<? extends Entry<? super K, ? super V>> entrySet(Object entity) {
        return $entrySet((T)entity);
    }

    @SuppressWarnings("unchecked")
    @Override
    public int size(Object entity) {
        return $size((T)entity);
    }


    public abstract boolean $containsKey(T entity, Object key);
    public abstract V $get(T entity, Object key);
    public abstract void $remove(T entity, Object key);
    public abstract void $clear(T entity);
    public abstract void $put(T entity, K key, V value);
    public abstract Set<? super K> $keySet(T entity);
    public abstract Set<? extends Entry<? super K, ? super V>> $entrySet(T entity);
    public abstract int $size(T entity);
}
