package org.aksw.jena_sparql_api.mapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

public class AggObject<K> implements Agg<Map<K, ?>> {
    private Map<K, Agg<?>> keyToSubAgg;

    public AggObject() {
        this(new HashMap<K, Agg<?>>());
    }

    public AggObject(Map<K, Agg<?>> keyToSubAgg) {
        this.keyToSubAgg = keyToSubAgg;
    }

    @Override
    public Acc<Map<K, ?>> createAccumulator() {
        Map<K, Acc<?>> keyToSubAcc = new HashMap<K, Acc<?>>();

        for (Entry<K, Agg<?>> entry : keyToSubAgg.entrySet()) {
            K key = entry.getKey();
            Agg<?> agg = entry.getValue();
            Acc<?> acc = agg.createAccumulator();

            keyToSubAcc.put(key, acc);
        }

        Acc<Map<K, ?>> result = new AccObject<K>(keyToSubAcc);
        return result;
    }

    @Override
    public Set<Var> getDeclaredVars() {
        // TODO Auto-generated method stub
        return null;
    }

    public static <K> AggObject<K> create(Map<K, Agg<?>> keyToSubAgg) {
        AggObject<K> result = new AggObject<K>(keyToSubAgg);
        return result;
    }
}
