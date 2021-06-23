package org.aksw.jena_sparql_api.lookup;

/**
 * This class needs a complete revision.
 * 
 * The design principles are:
 * - join multiple requests for the same key into at most n requests; with n at least 1
 * - cancellation of a request does not cancel the effective request if any of the retrieved keys are still referenced
 * - conversely, support a flag for whether effective requests without any referenced keys should be cancelled.
 * 
 * Issues: How to group requests?
 * - try to retain locality of keys by original request
 * - use a global pool
 */


//class RequestContext<K, V> {
//	protected Map<K, CompletableFuture<V>> futures;
//	protected Disposable exection;
//	
//	
//	// The (excerpt of) futures to be resolved upon completion of the request
//	public Map<K, CompletableFuture<V>> getFutures() {
//		return futures;
//	}
//	
//	public void addKey(K key) {
//		
//	}
//	
//	public Set<K> getKeys() {
//		return null;
//	}
//	
//	public CompletableFuture<V> getFuture() {
//		return future;
//	}
//
//
//	/**
//	 * Cancels an associate request execution. Any requested key
//	 */
//	public void cancel() {
//		
//	}
//}
//
//
//
///**
// * A lookup service that manages and possibly merges requests for keys.
// * Each key is associated with the set of requests contexts
// * 
// * 
// * 
// * Each set of requested keys creates a request object. 
// * 
// * If a request is cancelled, all keys referenced by other requests may be used to 'extend' these other requests.
// * Extend means, that even if base.apply() has already been called, a further request to base.apply(...) is performed
// * 
// * 
// * @author raven
// *
// */
//public class LookupServiceRequestCache<K, V> {
//
//	protected Cache<K, RequestContext<K, V>> requestCache;
//	
//	protected int defaultRequestCapacity;
//	
//	// Request being built using apply() but which have not yet been executed and thus
//	// allow adding more keys to be requested
//	protected Set<RequestContext<K, V>> openContexts = new LinkedHashSet<>(); // TODO we want a LinkedIdentityHashSet
//	
//	protected Multimap<K, RequestContext<K, V>> keyToCtxs;
//	
//
//    ///protected Map<K, RequestContext<K, V> requestCache;
////    protected Multimap<K, Object> producer;
////    protected Multimap<K, Object> consumers;
//    
//    
//    protected Set<K> buffer = new LinkedHashSet<>();
//    
//    private LookupService<K, V> base;
//
//    
//    
//    public LookupServiceRequestCache(LookupService<K, V> base) {
//        this(base, 10000);
//    }
//
//    public LookupServiceRequestCache(LookupService<K, V> base, long maxCacheSize) {
//        this(base, CacheBuilder.newBuilder().maximumSize(maxCacheSize).build());
//    }
//
//    public LookupServiceRequestCache(LookupService<K, V> base, Cache<K, Entry<CompletableFuture<V>, Set<Object>>> cache) {
//        this.base = base;
//        this.cache = cache;
//        
//        this.producers = HashMultimap.create();
//        this.consumers = HashMultimap.create();
//    }
//
//    /**
//     * Iterate the keys and check which ones have running requests and which ones need to
//     * trigger new requests
//     * 
//     * @param keys
//     */
//    public void processKeys(Iterable<K> keys) {
//    	
//    	
//
//    	ImmutableMap<K, RequestContext<K, V>> snapshot = requestCache.getAllPresent(keys);
//
//    	
//    	// The new request queue groups together keys 
//    	PublishProcessor<K> newRequestQueue = PublishProcessor.create();
//    	
//    	Disposable disposable = newRequestQueue
////    		.buffer(set -> set.size() >= 20, () -> new HashSet<K>())
//    		.buffer(20)
//    		.flatMap(this::doRequest)
//    		.doOnCancel(() -> {
//    			// If the queue is cancelled, 
//    		})
//    		.subscribe(e -> emitter.onNext(Maps.immutableEntry(e.getKey(), e.getValue())));
//
//    	
//    }
//
//    // keys -> originalRequest
//    // keys -> modifiedRequests -> execution
//    
//    /**
//     * Mark a key as being requested.
//     * 
//     * @param ctx
//     * @param key
//     */
//    public synchronized void enqueueRequest(RequestContext<K, V> ctx, K key) {
//    	ctx.addKey(key);
//    	keyToCtxs.put(key, ctx);
//    }
//    
//    
//    // request context state: NEW, OPEN, EXECUTING, DONE
//    
//    public synchronized void enqueue(K key) {
//    	
//    	
//    	
//    	keyToCtxs.get(key);
//    	
//    	// Check whether the key can be merged into an open context; i.e.
//    	// a context currently being built by an explicit request using .apply(...)
//    	for(RequestContext<K, V> ctx : openCtxs) {
//    		ctx.lock();
//    		
//    		
//    		ctx.unlock();
//    	}
//    }
//    
//    
//    public void exec(RequestContext<K, V> ctx) {
//    	Set<K> keys = ctx.getKeys();
//    	Flowable<Entry<K, V>> flowable = base.apply(keys);
//    	
//    	Map<K, CompletableFuture<V>> futures = ctx.getFutures();
//    	
//    	Disposable d = flowable.subscribe(e -> {
//    		K k = e.getKey();
//    		V v = e.getValue();
//
//    		CompletableFuture<V> future = futures.get(k);
//    		Objects.requireNonNull(future, "NULL future - should not happen");
//    		future.complete(v);    		
//    	});
//    }
//    
//    public synchronized void onCancelCtx(RequestContext<K, V> requestCtx) {
//    	Set<K> keys = requestCtx.getKeys();
//
//    	Set<K> remainingKeys = requestCtx.getFutures().entrySet().stream()
//    		.filter(e -> e.getValue().isDone())
//    		.map(Entry::getKey)
//    		.collect(Collectors.toSet());
//    	
//    	// If the requestCtx was the last one 'active', make an inactive context active
//    	// If there is no last active one,
//    	for(K key : keys) {
//    		Collection<RequestContext<K, V>> keyCtxs = keyToCtxs.get(key);
//    		keyCtxs.remove(requestCtx);    		
//    	}
//    	
//    	enqueue(remainingKeys);
//    }
//
//    
//
//    
//    public Flowable<Entry<K, V>> apply(Iterable<K> keys) {
//    	
//    	
//    	
//    	
//    	return Flowable.create(emitter -> {
//
//
//    		
//
//        	// Cancel the queue  if the emitter is cancelled
//    		emitter.setCancellable(disposable::dispose);
//
//    		
//    		Iterator<K> it = keys.iterator();
//    		
//    		// Iterate the keys - if it is cached, yield it, otherwise, enqueue the request
//    		while(!emitter.isCancelled() && it.hasNext()) {
//    			K key = it.next();
//    		
//    			// Get the sets of request contexts that contain this key
//    			RequestContext<K, V> ctx = requestCache.getIfPresent(key);
//    			
//    			
//    			// If there is no prior context, append the key to queue for new requests
//    			if(ctx == null) {
//    				newRequestQueue.onNext(k);
//    			} else {
//    				// Otherwise, 
//    				
//    			}
//
//    			// Whenever the value gets ready, emit it
//    			ctx.getFuture().thenAccept(v -> emitter.onNext(Maps.immutableEntry(key, v)));    			
//    		}
//
//    		queue.doOnComplete(emitter::onComplete);
//    		
//    	}, BackpressureStrategy.BUFFER);
//    }
//
//
//
//
//    public Flowable<Entry<K, V>> doRequest(List<K> keys) {
//    	
//    	
//    	Set<K> openKeys = new LinkedHashSet<>(keys);
//    	
//    	// Mark all keys provided as arguments as being produced and consumed by this request
//        Object token = new Object();
//    	for(K key : keys) {
//	    	producers.put(key, token);
//	    	consumers.put(key, token);
//    	}
//
//    	Flowable<Entry<K, V>> result = base.apply(keys)
//    			.doOnNext(e -> {
//    				K k = e.getKey();
//    				V v = e.getValue();
//    			
//    				// If a key was loaded successfully, be done with it
//        			CompletableFuture<V> future = pending.get(k);
//        			future.complete(v);
//
//        			openKeys.remove(k);
//    				producers.remove(k, token);
//    				consumers.remove(k, token);
//    			})
//    			.doOnCancel(() -> {
//    				// Get the list of all unprocessed keys that still have consumers
//    				// but no more producers
//    				
//    				for(K k : openKeys) {
//    					Collection<Object> ps = producers.get(k);
//    					Collection<Object> cs = consumers.get(k);
//    					
//    					ps.remove(token);
//    					cs.remove(token);
//    					
//    					
//    				}
//    				
//    				
//    			});
//    	
//    	return result;
//    }
//    
//    public void enqueue(K key, BiConsumer<K, V> action) {
//    	PublishProcessor<K> queue = PublishProcessor.create();
//
//
//    	
//    	queue.onNext(key);
//    	
//    	queue
//    		.buffer(20)
//    		.flatMap(this::doRequest)
//    		.subscribe(e -> {
//    			K k = e.getKey();
//    			V v = e.getValue();
//    			
//    			action.accept(k, v);
//    		});
//    	
//    	
////    	CompletableFuture<V> future = pending.computeIfAbsent(key, k -> {
////    		CompletableFuture<V> r = new CompletableFuture<>();
////    		r.thenAccept(v -> action.accept(k, v));
////    		return r;
////    	});
////    	
////    	buffer.add(key);
////    	
////    	int bufferRequestSize = 20;
////    	if(buffer.size() >= bufferRequestSize) {
////    		
////    	}
//    }
//    
//    
//    public void waitForCompletion(Runnable action) {
//    	queue
//    }
//    
//    
//
//    public static <K, V> LookupServiceCacheMem<K, V> create(LookupService<K, V> base) {
//        LookupServiceCacheMem<K, V> result = new LookupServiceCacheMem<K, V>(base);
//        return result;
//    }
//
//    public static <K, V> LookupServiceCacheMem<K, V> create(LookupService<K, V> base, int maxCacheSize) {
//        LookupServiceCacheMem<K, V> result = new LookupServiceCacheMem<K, V>(base, maxCacheSize);
//        return result;
//    }
//
//    public static <K, V> LookupServiceCacheMem<K, V> create(LookupService<K, V> base, Map<K, V> cache) {
//        LookupServiceCacheMem<K, V> result = new LookupServiceCacheMem<K, V>(base, cache);
//        return result;
//    }
//}
