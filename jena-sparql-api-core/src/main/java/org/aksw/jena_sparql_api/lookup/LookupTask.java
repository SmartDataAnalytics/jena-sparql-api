package org.aksw.jena_sparql_api.lookup;

import java.util.Map;
import java.util.concurrent.Callable;

public class LookupTask<K, V>
    implements Callable<Map<K, V>>
{
    private LookupService<K, V> base;
    private Iterable<K> keys;
    
    public LookupTask(LookupService<K, V> base, Iterable<K> keys) {
        this.base = base;
        this.keys = keys;
    }
    
    @Override
    public Map<K, V> call() throws Exception {
        Map<K, V> result = base.lookup(keys);
        return result;
    }   
}