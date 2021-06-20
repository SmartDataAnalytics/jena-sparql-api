package org.aksw.jena_sparql_api.utils.views.map;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collections.ConvertingCollection;
import org.aksw.commons.collections.sets.SetFromCollection;

import com.google.common.base.Converter;

public class MapFromKeyConverter<K, J, V>
    extends AbstractMap<K, V>
{
    protected Map<J, V> map;
    protected Converter<J, K> converter;

    public MapFromKeyConverter(Map<J, V> map, Converter<J, K> converter) {
        super();
        this.map = map;
        this.converter = converter;
    }

    @Override
    public V put(K key, V value) {
        J j = converter.reverse().convert(key);
        V result = map.put(j, value);
        return result;
    }

    @Override
    public V get(Object key) {
        V result;
        // If we fail to convert the key, we implicitly assume it is not contained
        try {
            J j = converter.reverse().convert((K)key);
            result = map.get(j);
        } catch(Exception e) {
            result = null;
        }
        return result;
    }

    @Override
    public boolean containsKey(Object key) {
        boolean result;
        // If we fail to convert the key, we implicitly assume it is not contained
        try {
            J j = converter.reverse().convert((K)key);
            result = map.containsKey(j);
        } catch(Exception e) {
            result = false;
        }
        return result;
    }

    @Override
    public V remove(Object key) {
        J j = converter.reverse().convert((K)key);
        V result = map.remove(j);
        return result;
    }

    @Override
    public Set<K> keySet() {
        return new SetFromCollection<>(new ConvertingCollection<>(map.keySet(), converter));
    }

    //public static <K, L> Entry<L, V> transform(Entry<K, V>, Function<? super K, ? extends V> )

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new SetFromCollection<>(new ConvertingCollection<>(map.entrySet(), new EntryConverterByKey<>(converter)));
    }
}
