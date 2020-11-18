package org.aksw.jena_sparql_api.lookup;

import java.util.Map.Entry;
import java.util.function.Function;

import io.reactivex.rxjava3.core.Flowable;

public class LookupServiceFlowableTransformer<K, I, O>
    implements LookupService<K, O>
{
    protected LookupService<K, I> delegate;
    protected Function<Flowable<Entry<K, I>>, Flowable<Entry<K, O>>> transform;

    public LookupServiceFlowableTransformer(LookupService<K, I> delegate,
            Function<Flowable<Entry<K, I>>, Flowable<Entry<K, O>>> transform) {
        super();
        this.delegate = delegate;
        this.transform = transform;
    }

    @Override
    public Flowable<Entry<K, O>> apply(Iterable<K> keys) {
        Flowable<Entry<K, I>> base = delegate.apply(keys);
        Flowable<Entry<K, O>> result = transform.apply(base);
        return result;
    }
}
