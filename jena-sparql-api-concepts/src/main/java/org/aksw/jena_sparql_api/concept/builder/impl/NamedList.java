package org.aksw.jena_sparql_api.concept.builder.impl;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.IntStream;

public class NamedList<K, V>
    implements Iterable<Entry<K, V>>
{
    protected List<V> values;
    protected Map<Integer, K> iToK;
    protected Map<K, V> kToV;

    public NamedList() {
        this.values = new ArrayList<>();
        this.iToK = new HashMap<>();
        this.kToV = new HashMap<>();
    }

    public void add(V value) {
        values.add(value);
    }

    public void add(K key, V value) {
        if(kToV.containsKey(key)) {
            throw new RuntimeException("Key " + key + " already exists");
        }

        int i = values.size();
        kToV.put(key, value);
        iToK.put(i, key);
        values.add(value);
    }

    public V get(K key) {
        V result = kToV.get(key);
        return result;
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        Iterator<Entry<K, V>> result = IntStream.range(0, values.size())
            .mapToObj(i -> {
                K k = iToK.get(i);
                V v = values.get(i);
                Entry<K, V> r = new SimpleEntry<>(k, v);
                return r;
            })
            .iterator();

        return result;
    }
}
