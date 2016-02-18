package org.aksw.jena_sparql_api.lookup;

import java.util.Map;
import java.util.function.Function;

public interface LookupService<K, V>
    extends Function<Iterable<K>, Map<K, V>>
{
//    Map<K, V> lookup(Iterable<K> keys);
}
