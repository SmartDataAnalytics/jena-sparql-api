package org.aksw.jena_sparql_api.lookup;

import java.util.Map.Entry;
import java.util.function.Function;

import com.google.common.collect.Range;

import io.reactivex.rxjava3.core.Flowable;

public class LookupServiceFromMapService<K, V, C>
    implements LookupService<K, V>
{
    protected MapService<C, K, V> mapService;
    protected Function<? super Iterable<? extends K>, C> keysToFilter;


    public LookupServiceFromMapService(MapService<C, K, V> mapService,
            Function<? super Iterable<? extends K>, C> keysToFilter) {
        super();
        this.mapService = mapService;
        this.keysToFilter = keysToFilter;
    }


    @Override
    public Flowable<Entry<K, V>> apply(Iterable<K> t) {
        C filter = keysToFilter.apply(t);
        return mapService.streamData(filter, Range.atLeast(0l));
    }

}