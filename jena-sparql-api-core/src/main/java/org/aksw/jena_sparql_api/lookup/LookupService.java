package org.aksw.jena_sparql_api.lookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface LookupService<K, V>
    extends Function<Iterable<K>, Map<K, V>>
{
    default LookupService<K, V> partition(int k) {
        return LookupServicePartition.create(this, k);
    }

    default <W> LookupService<K, W> mapValues(BiFunction<K, V, W> fn) {
        return LookupServiceTransformValue.create(this, fn);
    }

    default List<V> fetchList(Iterable<K> keys) {
        Collection<V> tmp = apply(keys).values();
        List<V> result = tmp instanceof List ? (List<V>)tmp : new ArrayList<V>(tmp);
        return result;
    }

//    @Override
//    default LookupService<K, V> andThen(

//    Map<K, V> lookup(Iterable<K> keys);
}
