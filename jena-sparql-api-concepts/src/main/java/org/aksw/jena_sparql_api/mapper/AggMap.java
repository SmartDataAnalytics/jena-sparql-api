package org.aksw.jena_sparql_api.mapper;

import java.util.Map;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

import com.google.common.collect.Sets;

/**
 * A less general form of AggMap2 ; this class should be removed and AggMap2 renamed to this
 * 
 * @author raven
 *
 * @param <K>
 * @param <V>
 */
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
        Set<Var> a = mapper instanceof BindingMapperVarAware<?>
        ? ((BindingMapperVarAware<?>)mapper).getVarsMentioned()
        : null // Collections.emptySet()
        ;

        Set<Var> b = subAgg.getDeclaredVars();
        Set<Var> result = a == null || b == null
            ? null
            : Sets.union(a, b);

        return result;
    }

    public static <K, V> AggMap<K, V> create(BindingMapper<K> mapper, Agg<V> subAgg) {
        AggMap<K, V> result = new AggMap<K, V>(mapper, subAgg);
        return result;
    }

}
