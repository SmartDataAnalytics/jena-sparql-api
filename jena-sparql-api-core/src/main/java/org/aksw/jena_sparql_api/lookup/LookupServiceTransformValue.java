package org.aksw.jena_sparql_api.lookup;

import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.jena.ext.com.google.common.collect.Maps;

import io.reactivex.rxjava3.core.Flowable;


public class LookupServiceTransformValue<K, W, V>
    implements LookupService<K, W>
{
    private LookupService<K, V> base;
    private BiFunction<? super K, ? super V, W> fn;

    public LookupServiceTransformValue(LookupService<K, V> base, BiFunction<? super K, ? super V, W> fn) {
        this.base = base;
        this.fn = fn;
    }

    @Override
    public Flowable<Entry<K, W>> apply(Iterable<K> keys) {
        Flowable<Entry<K, V>> tmp = base.apply(keys);
        Flowable<Entry<K, W>> result = tmp.map(e-> Maps.immutableEntry(e.getKey(), fn.apply(e.getKey(), e.getValue())));

//        Map<K, W> result = tmp.entrySet().stream()
//                .collect(Collectors.toMap(
//                        e -> e.getKey(),
//                        e -> fn.apply(e.getKey(), e.getValue()),
//                        (u, v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); },
//                        LinkedHashMap::new));
//
//
//        //Maps.transformValues(tmp, GuavaFunctionWrapper.wrap(fn));
        return result;
    }

    //@Deprecated
    /**
     * create method that only passes the original value to the mapping function
     *
     * @param base
     * @param fn
     * @return
     */
    public static <K, W, V> LookupServiceTransformValue<K, W, V> create(LookupService<K, V> base, BiFunction<? super K, ? super V, W> fn) {
        LookupServiceTransformValue<K, W, V> result = new LookupServiceTransformValue<K, W, V>(base, fn);
        return result;
    }

    /**
     * create method tha passes both key and value of each entry to the mapping function
     *
     * @param base
     * @param fn
     * @return
     */
    public static <K, W, V> LookupServiceTransformValue<K, W, V> create(LookupService<K, V> base, Function<? super V, W> fn) {
        LookupServiceTransformValue<K, W, V> result = new LookupServiceTransformValue<K, W, V>(base, (k, v) -> fn.apply(v));
        return result;
    }
}
