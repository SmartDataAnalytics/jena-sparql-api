package org.aksw.jena_sparql_api.lookup;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

/**
 * Map a stream of items to a stream of entries
 *
 * @author raven
 *
 * @param <I>
 * @param <K>
 * @param <V>
 */
public interface EntryService<K, V>
    extends Function<Collection<K>, Map<K, V>>//Function<Stream<K>, Stream<Entry<K, V>>>
{
//    default Map<K, V> toMap(Stream<K> in) {
//        Map<K, V> result = apply(in)
//                .collect(Collectors.toMap(
//                        Entry::getKey,
//                        Entry::getValue,
//                        (u, v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); },
//                        LinkedHashMap::new));
//        return result;
//    }
}
