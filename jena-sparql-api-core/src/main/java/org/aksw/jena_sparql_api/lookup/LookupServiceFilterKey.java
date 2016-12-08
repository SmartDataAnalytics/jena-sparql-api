package org.aksw.jena_sparql_api.lookup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Predicate;

public class LookupServiceFilterKey<K, V>
    implements LookupService<K, V>
{
    protected LookupService<K, V> delegate;
    protected Predicate<K> filter;

    public LookupServiceFilterKey(LookupService<K, V> delegate, Predicate<K> filter) {
        this.delegate = delegate;
        this.filter = filter;
    }

    @Override
    public Map<K, V> apply(Iterable<K> t) {
        List<K> keys = new ArrayList<>();
        for(K key : t) {
            boolean accept = filter.apply(key);
            if(accept) {
                keys.add(key);
            }
        }

        Map<K, V> result = delegate.apply(keys);
        return result;
    }
}
