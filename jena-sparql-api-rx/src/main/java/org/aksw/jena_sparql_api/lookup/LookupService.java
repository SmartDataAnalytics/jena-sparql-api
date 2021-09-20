package org.aksw.jena_sparql_api.lookup;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public interface LookupService<K, V>
    extends Function<Iterable<K>, Flowable<Entry<K, V>>> //CompletableFuture<Map<K, V>>>
{
    default V fetchItem(K k) {
        return fetchMap(Collections.singleton(k)).get(k);
    }

    default LookupService<K, V> partition(int k) {
        return LookupServicePartition.create(this, k);
    }

    default <W> LookupService<K, W> mapValues(BiFunction<K, V, W> fn) {
        return LookupServiceTransformValue.create(this, fn);
    }

    default <I> LookupService<I, V> mapKeys(Function<? super I, ? extends K> fn) {
        return LookupServiceTransformKey.create(this, fn);
    }

    default LookupService<K, V> cache() {
        return LookupServiceCacheMem.create(this);
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


    /**
     * A convenience short-hand for fetching a map
     * by first mapping the keys to proxy keys.
     *
     * Equivalent to
     * <pre>this.<X>mapKeys(keyMapper).fetchMap(keys)</pre>
     *
     * @param <X>
     * @param keys
     * @param keyToProxy
     * @return
     */
    default <X> Map<X, V> fetchMap(
            Iterable<X> keys,
            Function<? super X, ? extends K> keyMapper) {
        Map<X, V> result = this.<X>mapKeys(keyMapper).fetchMap(keys);
        return result;
    }


//    default <X> LookupService<X, V> fetchMapWithProxyKeys(Iterable<X> keys,
//            Function<? super X, ? extends K> keyToProxy) {
//        return fetchMapWithProxyKeys(keys, keyToProxy, this);
//    }
//
//    default <X> Map<X, V> fetchMapWithProxyKeys(Iterable<X> keys,
//            Function<? super X, ? extends K> keyToProxy) {
//        return fetchMapWithProxyKeys(keys, keyToProxy, this);
//    }
//
//    /**
//     *
//     *
//     * @param <K>
//     * @param <P>
//     * @param <V>
//     * @param keys
//     * @param keyToProxy
//     * @param delegate
//     * @return
//     */
//    static <K, P, V> Map<K, V> fetchMapWithProxyKeys(
//            Iterable<K> keys,
//            Function<? super K, ? extends P> keyToProxy,
//            LookupService<P, V> delegate) {
//        Multimap<P, ? super K> index = Multimaps.index(keys, keyToProxy::apply);
//
//        Set<P> proxyKeys = index.asMap().keySet();
//        Map<P, V> proxyKeyToValue = delegate.fetchMap(proxyKeys);
//
//        Map<K, V> result = Streams.stream(keys)
//            .collect(Collectors.toMap(key -> key, key -> {
//                P proxyKey = keyToProxy.apply(key);
//                V r = proxyKeyToValue.get(proxyKey);
//                return r;
//            }));
//
//        return result;
//    }
}
