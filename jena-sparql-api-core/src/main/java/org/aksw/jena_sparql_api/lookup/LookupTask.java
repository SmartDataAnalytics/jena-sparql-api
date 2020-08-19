package org.aksw.jena_sparql_api.lookup;

import java.util.Map.Entry;
import java.util.concurrent.Callable;

import io.reactivex.rxjava3.core.Flowable;

public class LookupTask<K, V>
    implements Callable<Flowable<Entry<K, V>>>
{
    private LookupService<K, V> base;
    private Iterable<K> keys;

    public LookupTask(LookupService<K, V> base, Iterable<K> keys) {
        this.base = base;
        this.keys = keys;
    }

    @Override
    public Flowable<Entry<K, V>> call() throws Exception {
        Flowable<Entry<K, V>> result = base.apply(keys);
        return result;
    }
}