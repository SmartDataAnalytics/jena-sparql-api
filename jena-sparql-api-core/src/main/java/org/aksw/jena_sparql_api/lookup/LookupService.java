package org.aksw.jena_sparql_api.lookup;

import java.util.Map;

import com.google.common.base.Function;

public interface LookupService<K, V>
    extends Function<Iterable<K>, Map<K, V>>
{
//    Map<K, V> lookup(Iterable<K> keys);
}
