package org.aksw.jena_sparql_api.lookup;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.processors.PublishProcessor;

class RequestContext<K, V> {
	protected R request;
	protected CompletableFuture<V> future;
	
	
	
	public CompletableFuture<V> getFuture() {
		return future;
	}


	/**
	 * Cancels the request. Any requested key
	 */
	public void cancel() {
		
	}
}


/**
 * A lookup service that manages and possibly merges requests for keys.
 * Each key is associated with the set of requests contexts
 * 
 * 
 * 
 * Each set of requested keys creates a request object. 
 * 
 * If a request is cancelled, all keys referenced by other requests may be used to 'extend' these other requests.
 * Extend means, that even if base.apply() has already been called, a further request to base.apply(...) is performed
 * 
 * 
 * @author raven
 *
 */
public class LookupServiceRequestCache<K, V> {

	protected Cache<K, RequestContext<K, V>> requestCache;
    ///protected Map<K, RequestContext<K, V> requestCache;
    protected Multimap<K, Object> producer;
    protected Multimap<K, Object> consumers;
    
    
    protected Set<K> buffer = new LinkedHashSet<>();
    
    private LookupService<K, V> base;

    
    
    public LookupServiceRequestCache(LookupService<K, V> base) {
        this(base, 10000);
    }

    public LookupServiceRequestCache(LookupService<K, V> base, long maxCacheSize) {
        this(base, CacheBuilder.newBuilder().maximumSize(maxCacheSize).build());
    }

    public LookupServiceRequestCache(LookupService<K, V> base, Cache<K, Entry<CompletableFuture<V>, Set<Object>>> cache) {
        this.base = base;
        this.cache = cache;
        
        this.producers = HashMultimap.create();
        this.consumers = HashMultimap.create();
    }

    public Flowable<Entry<K, V>> apply(Iterable<K> keys) {
    	return Flowable.create(emitter -> {

        	PublishProcessor<K> queue = PublishProcessor.create();
        	
        	Disposable disposable = queue
//        		.buffer(set -> set.size() >= 20, () -> new HashSet<K>())
        		.buffer(20)
        		.flatMap(this::doRequest)
        		.doOnCancel(() -> {
        			// If the queue is cancelled, 
        		})
        		.subscribe(e -> emitter.onNext(Maps.immutableEntry(e.getKey(), e.getValue())));


        	// Cancel the queue  if the emitter is cancelled
    		emitter.setCancellable(disposable::dispose);

    		
    		Iterator<K> it = keys.iterator();
    		
    		// Iterate the keys - if it is cached, yield it, otherwise, enqueue the request
    		while(!emitter.isCancelled() && it.hasNext()) {
    			K key = it.next();
    		
    			// Get the sets of request contexts that contain this key
    			RequestContext<K, V> ctx = requestCache.getIfPresent(key);
    			
    			
    			// If there is no prior context, append the key to queue for new requests
    			if(ctx == null) {
    				newRequestQueue.onNext(k);
    			} else {
    				// Otherwise, 
    				
    			}

    			// Whenever the value gets ready, emit it
    			ctx.getFuture().thenAccept(v -> emitter.onNext(Maps.immutableEntry(key, v)));    			
    		}

    		queue.doOnComplete(emitter::onComplete);
    		
    	}, BackpressureStrategy.BUFFER);
    }
    
    public Flowable<Entry<K, V>> doRequest(List<K> keys) {
    	Set<K> openKeys = new LinkedHashSet<>(keys);
    	
    	// Mark all keys provided as arguments as being produced and consumed by this request
        Object token = new Object();
    	for(K key : keys) {
	    	producers.put(key, token);
	    	consumers.put(key, token);
    	}

    	Flowable<Entry<K, V>> result = base.apply(keys)
    			.doOnNext(e -> {
    				K k = e.getKey();
    				V v = e.getValue();
    			
    				// If a key was loaded successfully, be done with it
        			CompletableFuture<V> future = pending.get(k);
        			future.complete(v);

        			openKeys.remove(k);
    				producers.remove(k, token);
    				consumers.remove(k, token);
    			})
    			.doOnCancel(() -> {
    				// Get the list of all unprocessed keys that still have consumers
    				// but no more producers
    				
    				for(K k : openKeys) {
    					Collection<Object> ps = producers.get(k);
    					Collection<Object> cs = consumers.get(k);
    					
    					ps.remove(token);
    					cs.remove(token);
    					
    					
    				}
    				
    				
    			});
    	
    	return result;
    }
    
    public void enqueue(K key, BiConsumer<K, V> action) {
    	PublishProcessor<K> queue = PublishProcessor.create();


    	
    	queue.onNext(key);
    	
    	queue
    		.buffer(20)
    		.flatMap(this::doRequest)
    		.subscribe(e -> {
    			K k = e.getKey();
    			V v = e.getValue();
    			
    			action.accept(k, v);
    		});
    	
    	
//    	CompletableFuture<V> future = pending.computeIfAbsent(key, k -> {
//    		CompletableFuture<V> r = new CompletableFuture<>();
//    		r.thenAccept(v -> action.accept(k, v));
//    		return r;
//    	});
//    	
//    	buffer.add(key);
//    	
//    	int bufferRequestSize = 20;
//    	if(buffer.size() >= bufferRequestSize) {
//    		
//    	}
    }
    
    
    public void waitForCompletion(Runnable action) {
    	queue
    }
    
    

    public static <K, V> LookupServiceCacheMem<K, V> create(LookupService<K, V> base) {
        LookupServiceCacheMem<K, V> result = new LookupServiceCacheMem<K, V>(base);
        return result;
    }

    public static <K, V> LookupServiceCacheMem<K, V> create(LookupService<K, V> base, int maxCacheSize) {
        LookupServiceCacheMem<K, V> result = new LookupServiceCacheMem<K, V>(base, maxCacheSize);
        return result;
    }

    public static <K, V> LookupServiceCacheMem<K, V> create(LookupService<K, V> base, Map<K, V> cache) {
        LookupServiceCacheMem<K, V> result = new LookupServiceCacheMem<K, V>(base, cache);
        return result;
    }
}
