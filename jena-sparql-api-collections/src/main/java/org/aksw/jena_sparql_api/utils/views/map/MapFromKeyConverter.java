package org.aksw.jena_sparql_api.utils.views.map;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.aksw.commons.collections.ConvertingCollection;
import org.aksw.commons.collections.sets.SetFromCollection;

import com.google.common.base.Converter;

class EntryConverterByKey<K, L, V>
    extends Converter<Entry<K, V>, Entry<L, V>>
{
    protected Converter<K, L> converter;

    public EntryConverterByKey(Converter<K, L> converter) {
        super();
        this.converter = converter;
    }

    @Override
    protected Entry<L, V> doForward(Entry<K, V> a) {
        return transformKey(a, converter::convert);
    }

    @Override
    protected Entry<K, V> doBackward(Entry<L, V> b) {
        return transformKey(b, converter.reverse()::convert);
    }

    public static <K, L, V> Converter<Entry<K, V>, Entry<L, V>> converterByKey(Converter<K, L> converter) {
        return Converter.from(a -> transformKey(a, converter::convert), b -> transformKey(b, converter.reverse()::convert));
    }

    public static <K, V, W> Converter<Entry<K, V>, Entry<K, W>> converterByValue(Converter<V, W> converter) {
        return Converter.from(a -> transformValue(a, converter), b -> transformValue(b, converter.reverse()));
    }



    public static <K, L, V> Entry<L, V> transformKey(Entry<K, V> e, Function<? super K, ? extends L> fn) {
        L key = fn.apply(e.getKey());
        return new Entry<L, V>() {
            @Override public L getKey() { return key; }
            @Override public V getValue() { return e.getValue(); }
            @Override public V setValue(V v) { return e.setValue(v); }
        };
    }

    public static <K, V, W> Entry<K, W> transformValue(Entry<K, V> e, Converter<V, W> converter) {
        K key = e.getKey();
        return new Entry<K, W>() {
            @Override public K getKey() { return key; }
            @Override public W getValue() {
                V raw = e.getValue();
                W r = converter.convert(raw);
                return r;
            }
            @Override public W setValue(W v) {
                V raw = converter.reverse().convert(v);
                e.setValue(raw);
                return v;
            }
        };
    }

}

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
