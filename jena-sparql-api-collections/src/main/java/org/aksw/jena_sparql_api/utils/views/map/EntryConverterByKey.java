package org.aksw.jena_sparql_api.utils.views.map;

import java.util.Map.Entry;
import java.util.function.Function;

import com.google.common.base.Converter;

/**
 * An extension of {@link Converter} specifically for converting {@link Entry} objects.
 *
 * @author raven
 *
 * @param <K> The source key type of the entry
 * @param <L> The target key type that is converted to
 * @param <V> The value type of the entry
 */
public class EntryConverterByKey<K, L, V>
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