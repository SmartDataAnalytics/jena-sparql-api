package org.aksw.jena_sparql_api.lookup;

import java.util.Map;
import java.util.function.Function;

public interface LookupService<K, V>
    extends Function<Iterable<K>, Map<K, V>>
{
    default LookupService<K, V> partition(int k) {
        return LookupServicePartition.create(this, k);
    }

    default <W> LookupService<K, W> mapValues(Function<V, W> fn) {
        return LookupServiceTransformValue.create(this, fn);
    }

//    @Override
//    default LookupService<K, V> andThen(

//    Map<K, V> lookup(Iterable<K> keys);
}
