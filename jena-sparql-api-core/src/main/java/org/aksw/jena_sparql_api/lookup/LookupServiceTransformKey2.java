package org.aksw.jena_sparql_api.lookup;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.jena_sparql_api.batch.F_NodeModelToResource;
import org.aksw.jena_sparql_api.batch.F_ResourceToNode;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;


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
    public Map<KI, V> apply(Iterable<KI> keys) {
        Iterable<KO> kos = Iterables.transform(keys, to);
        Map<KO, V> tmp = delegate.apply(kos);


        Map<KI, V> result = new LinkedHashMap<KI, V>();
        for(Entry<KO, V> entry : tmp.entrySet()) {
            KI ki = from.apply(entry);
            V v = entry.getValue();
            result.put(ki, v);
        }

        return result;
    }

    public static <KI, KO, V> LookupServiceTransformKey2<KI, KO, V> create(LookupService<KO, V> delegate, Function<? super KI, KO> to, Function<? super Entry<KO, V>, KI> from) {
        LookupServiceTransformKey2<KI, KO, V> result = new LookupServiceTransformKey2<KI, KO, V>(delegate, to, from);
        return result;
    }
}
