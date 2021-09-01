package org.aksw.jena_sparql_api.utils.model;

import java.util.Map;
import java.util.Set;

class MapDiff<K, V> {
    protected Set<K> deletions;

    // upsert = insert or replacement
    protected Map<K, V> upserts;
}

// If we need such an implementation then the write transaction should only write to a MapDiff object
//public class TransactionalMapImpl<K, V>
//    extends AbstractMap<K, V>
//    implements TransactionalMap<K, V>
//{
// Create map views using MapUtils.union(MapUtils.difference(base, deletions), upserts)
//}
