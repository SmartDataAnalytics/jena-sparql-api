package org.aksw.jena_sparql_api.lookup;

import java.util.Map.Entry;

import org.apache.jena.ext.com.google.common.collect.Maps;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import io.reactivex.rxjava3.core.Flowable;


public class LookupServiceTransformKey2<KI, KO, V>
    implements LookupService<KI, V>
{
    private LookupService<KO, V> delegate;
    private Function<? super KI, KO> to;
    private Function<? super Entry<KO, V>, KI> from;

    public LookupServiceTransformKey2(LookupService<KO, V> delegate,
            Function<?super KI, KO> to, Function<? super Entry<KO, V>, KI> from) {
        super();
        this.delegate = delegate;
        this.to = to;
        this.from = from;
    }

    @Override
    public Flowable<Entry<KI, V>> apply(Iterable<KI> keys) {
        Iterable<KO> kos = Iterables.transform(keys, to);
        Flowable<Entry<KO, V>> tmp = delegate.apply(kos);

        Flowable<Entry<KI, V>> result = tmp.map(entry -> {
            KI ki = from.apply(entry);
            V v = entry.getValue();
            return Maps.immutableEntry(ki, v);
        });

//        Map<KI, V> result = new LinkedHashMap<KI, V>();
//        for(Entry<KO, V> entry : tmp.entrySet()) {
//            KI ki = from.apply(entry);
//            V v = entry.getValue();
//            result.put(ki, v);
//        }

        return result;
    }

    public static <KI, KO, V> LookupServiceTransformKey2<KI, KO, V> create(LookupService<KO, V> delegate, Function<? super KI, KO> to, Function<? super Entry<KO, V>, KI> from) {
        LookupServiceTransformKey2<KI, KO, V> result = new LookupServiceTransformKey2<KI, KO, V>(delegate, to, from);
        return result;
    }
}
