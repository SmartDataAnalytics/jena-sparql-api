package org.aksw.jena_sparql_api.lookup;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.reactivex.Flowable;
import io.reactivex.Single;

public interface LookupService<K, V>
    extends Function<Iterable<K>, Flowable<Entry<K, V>>> //CompletableFuture<Map<K, V>>>
{
    default LookupService<K, V> partition(int k) {
        return LookupServicePartition.create(this, k);
    }

    default <W> LookupService<K, W> mapValues(BiFunction<K, V, W> fn) {
        return LookupServiceTransformValue.create(this, fn);
    }

    /**
     * Requests a map.
     * 
     * The 'Single' result type can be seen as representing the request.
     * 
     * @param keys
     * @return
     */
    default Single<Map<K, V>> requestMap(Iterable<K> keys) {
    	Single<Map<K, V>> result = apply(keys)
    			.toMap(Entry::getKey, Entry::getValue);

        return result;
    }

    default Map<K, V> fetchMap(Iterable<K> keys) {
    	Map<K, V> result = requestMap(keys).blockingGet();
        return result;
    }

    default Single<List<V>> requestList(Iterable<K> keys) {
    	Single<List<V>> result = apply(keys)
    			.map(Entry::getValue)
    			.toList();
        
    	return result;
    }

    default List<V> fetchList(Iterable<K> keys) {
    	List<V> result = requestList(keys).blockingGet();
        return result;
    }
}
