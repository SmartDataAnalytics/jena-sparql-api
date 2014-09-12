package org.aksw.jena_sparql_api.mapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.function.FunctionEnv;


public class AccMap<K, V>
    implements Acc<Map<K, V>> 
{
    private BindingMapper<K> mapper;
    private Agg<V> subAgg;

    private Map<K, Acc<V>> state;

    public AccMap(BindingMapper<K> mapper, Agg<V> subAgg) {
        this.mapper = mapper;
        this.subAgg = subAgg;
    }

    @Override
    public void accumulate(Binding binding) {
        // TODO Keep track of the relative binding index
        K k = mapper.map(binding,-1);
        Acc<V> subAcc = state.get(k);
        if(subAcc == null) {
            subAcc = subAgg.createAccumulator();
            state.put(k, subAcc);
        }
        subAcc.accumulate(binding);
    }

    @Override
    public Map<K, V> getValue() {
        Map<K, V> result = new HashMap<K, V>();

        for(Entry<K, Acc<V>> entry : state.entrySet()) {
            K k = entry.getKey();
            V v = entry.getValue().getValue();
            
            result.put(k, v);
        }
        
        return result;
    }

}
