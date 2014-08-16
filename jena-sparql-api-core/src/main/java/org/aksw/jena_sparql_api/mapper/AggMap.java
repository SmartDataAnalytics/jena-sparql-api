package org.aksw.jena_sparql_api.mapper;

import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.sparql.core.Var;

public class AggMap<K, V>
    implements Agg<Map<K,V>>
{
    private BindingMapper<K> mapper;
    private Agg<V> subAgg;

    public AggMap(BindingMapper<K> mapper, Agg<V> subAgg) {
        this.mapper = mapper;
        this.subAgg = subAgg;
    }

    @Override
    public Acc<Map<K, V>> createAccumulator() {
        Acc<Map<K, V>> result = new AccMap<K, V>(mapper, subAgg);
        return result;
    }

    @Override
    public Set<Var> getDeclaredVars() {
        // TODO Auto-generated method stub
        return null;
    }

    public static <K, V> AggMap<K, V> create(BindingMapper<K> mapper, Agg<V> subAgg) {
        AggMap<K, V> result = new AggMap<K, V>(mapper, subAgg);
        return result;
    }

}
