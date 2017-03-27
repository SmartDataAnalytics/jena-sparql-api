package org.aksw.jena_sparql_api.mapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;

/**
 * An ggregator that initialized with a static map<Key, Aggregator<...>
 *
 * Every key in the map is passed to a function that yields the value to be passed as the new binding to the child accumulator.
 * Hence, this accumulator is 'map'-centric as its the map's keys that drive the lookup
 *
 * TODO Clarify relation between AggObject, AggMap and AggStaticMultiplex
 *
 * @author raven
 *
 * @param <K>
 * @param <V>
 */
public class AccStaticMultiplex<B, K, U, V>
    implements Accumulator<B, Map<K, V>> {
    //protected Function<B, ? extends Iterable<? extends K>> bindingToKeys;

    /**
     * Obtain the childBinding based on the current binding and the current key of the map
     */
    protected BiFunction<B, K, ? extends U> childBinding;
    protected Map<K, Accumulator<U, V>> keyToSubAcc;

    public AccStaticMultiplex(BiFunction<B, K, ? extends U> childBinding, Map<K, Accumulator<U, V>> keyToSubAcc) {
        this.childBinding = childBinding;
        this.keyToSubAcc = keyToSubAcc;
    }

    @Override
    public void accumulate(B binding) {
        for(Entry<K, Accumulator<U, V>> e : keyToSubAcc.entrySet()) {
            K k = e.getKey();
            Accumulator<? super U, V> subAcc = e.getValue();

            U u = childBinding.apply(binding, k);

            subAcc.accumulate(u);
        }
    }

    @Override
    public Map<K, V> getValue() {
        Map<K, V> result = new HashMap<>(keyToSubAcc.size());
        for(Entry<K, Accumulator<U, V>> e : keyToSubAcc.entrySet()) {
            K k = e.getKey();
            Accumulator<U, V> subAcc = e.getValue();
            V v = subAcc.getValue();

            result.put(k, v);
        }

        return result;
    }

    public static <B, K, U, V> Accumulator<B, Map<K, V>> create(BiFunction<B, K, ? extends U> childBinding, Map<K, Accumulator<U, V>> keyToSubAcc) {
        AccStaticMultiplex<B, K, U, V> result = new AccStaticMultiplex<>(childBinding, keyToSubAcc);
        return result;
    }
}
