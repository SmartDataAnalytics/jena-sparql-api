package org.aksw.jena_sparql_api.batch.backend.sparql;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.jena_sparql_api.beans.model.MapOpsBase;


public class MapOpsMap<K, V>
    extends MapOpsBase<Map<? super K, ? super V>, K, V>
{
    public MapOpsMap(Class<K> keyClass, Class<V> valueClass) {
        super(Map.class, keyClass, valueClass);
    }

    @Override
    public boolean $containsKey(Map<? super K, ? super V> entity, Object key) {
        boolean result = entity.containsKey(key);
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V $get(Map<? super K, ? super V> entity, Object key) {
        Object result = entity.get(entity);
        return (V)result;
    }

    @Override
    public void $remove(Map<? super K, ? super V> entity, Object key) {
        entity.remove(key);
    }

    @Override
    public void $clear(Map<? super K, ? super V> entity) {
        entity.clear();
    }

    @Override
    public void $put(Map<? super K, ? super V> entity, K key, V value) {
        entity.put(key, value);
    }

    @Override
    public Set<? super K> $keySet(Map<? super K, ? super V> entity) {
        return entity.keySet();
    }

    @Override
    public int $size(Map<? super K, ? super V> entity) {
        return entity.size();
    }

    @Override
    public Set<? extends Entry<? super K, ? super V>> $entrySet(
            Map<? super K, ? super V> entity) {
        return entity.entrySet();
    }

}
