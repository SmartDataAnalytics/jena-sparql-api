package org.aksw.commons.rx.cache.range;

import java.util.Collections;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;

import org.aksw.commons.util.ref.Ref;
import org.aksw.jena_sparql_api.lookup.ListPaginator;
import org.aksw.jena_sparql_api.rx.util.collection.RangedSupplier;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

interface PageManager<T> {
	// Reference<? extends Page<T>>
	Page<T> getPage(long pageId);
	int getPageSize();
}


interface Page<T> {
	long getOffset();
	PageManager<T> getPageManager();

	T get(int offset);
	void set(int offset, T value);
	int getKnownSize();
	
	default Page<T> getNextPage() {
		return getPageManager().getPage(getOffset() + 1);
	}
}





class RangeRequestContext<C extends Comparable<C>> {
	protected Range<C> range;
	
	public RangeRequestContext(Range<C> range) {
		super();
		this.range = range;
	}

	public Range<C> getRange() {
		return range;
	}
}


class ExecutorPool {
	// protected 
	protected Set<Range<Long>> requestRanges;
	
	
	public void addRequest(Range<Long> request) {
		
	}
	
}


public class SmartRangeCacheImpl<T>
	implements ListPaginator<T>
{
	protected RangedSupplier<Long, T> backend;
	
	protected int pageSize;
	protected ClaimingCache<Long, RangeBuffer<T>> pageCache;
	// protected SortedCache<Long, RangeBuffer<T>> pageCache;

	protected Set<RangeRequestExecutor<T>> executors = Collections.synchronizedSet(Sets.newIdentityHashSet());

	
	protected Set<RequestIterator<T>> activeRequests = Collections.synchronizedSet(Sets.newIdentityHashSet());
	
	protected ReentrantReadWriteLock executorCreationLock = new ReentrantReadWriteLock(true);

	protected volatile long knownSize = -1;
	
	public SmartRangeCacheImpl(RangedSupplier<Long, T> backend) {
		this.backend = backend;
		
		pageSize = 1024;
		
		pageCache = new ClaimingCache<>(
				CacheBuilder.newBuilder()
					.maximumSize(1000)
					.build(new CacheLoader<Long, RangeBuffer<T>>() {
						@Override
						public RangeBuffer<T> load(Long key) throws Exception {
							RangeBuffer<T> result = new RangeBuffer<T>(pageSize);
							onPageLoad(key, result);
							return result;
						}
					}),
				new TreeMap<>()
		);
	}
	
	public int getPageSize() {
		return pageSize;
	}
	
	
	/**
	 * Listener on page loads that auto-claims pages to RequestIterators
	 * Note that the listener is invoked before the entry can be registered at the cache -
	 * cache lookups must not be performed in the listener's flow of control.
	 */
	protected void onPageLoad(Long key, RangeBuffer<T> page) {
		
	}
	
	
	public Set<RangeRequestExecutor<T>> getExecutors() {
		return executors;
	}

	
	Lock getExecutorCreationReadLock() {
		return executorCreationLock.readLock();
	}

	
	public long getPageIdForOffset(long offset) {
		long result = offset % pageSize;
		return result;
	}

	/**
	 * This method should only be called by producers.
	 * 
	 * This method triggers loads of non-loaded pages which in turn
	 * 
	 * 
	 * */
	public Ref<RangeBuffer<T>> getPageForOffset(long offset) {
		long pageId = offset % pageSize;
		return getPageForPageId(pageId);
	}
	
	public Ref<RangeBuffer<T>> getPageForPageId(long pageId) {
		Ref<RangeBuffer<T>> result;
		try {
			result = pageCache.claim(pageId);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
		return result;		
	}
	
	public int getIndexInPageForOffset(long offset) {
		return (int)(offset % (long)pageSize);
	}
	
	
	protected void updateExecutors() {
		
	}
	
	public Runnable register(RequestIterator<T> it) {
		activeRequests.add(it);
		updateExecutors();
		
		return () -> {
			activeRequests.remove(it);
			updateExecutors();
		};
	}

	
	
	protected Set<BiConsumer<Long, RangeBuffer<T>>> pageLoadListeners = Collections.synchronizedSet(Sets.newIdentityHashSet());
	
	public Runnable addPageLoadListener(BiConsumer<Long, RangeBuffer<T>> listener) {
		pageLoadListeners.add(listener);
		return () -> pageLoadListeners.remove(listener);
	}
	
	
	/** The open requests sorted by the start of their lowest GAP! (not the original request offset)
	 *  I.e. requests are indexed by their first position where backend data retrieval must be performed
	 */
	// protected TreeMultimap<Long, RequestContext> openRequests;
	
	
	/**
	 * Map of the next interceptable executor offset to the executor
	 * Executors regularly synchronize on this map to declare the offset on which they check
	 * 
	 * 
	 */
	protected NavigableMap<Long, Executor> offsetToExecutor = new TreeMap<>();
	// protected ConcurrentNavigableMap<Long, Executor> offsetToExecutor = new ConcurrentSkipListMap<>();
	
	/// protected RangeMap<Long, Object> autoClaimers;
	
	
	public NavigableMap<Long, Ref<RangeBuffer<T>>> claimPages(Range<Long> requestRange) {

		
		
		
		return null;
	}
	
	/**
	 * Create a RequestContext for the given requestRange:
	 * 
	 * (1) Claim cached pages for the start-range of the request range
	 * (2) Check the running executors for whether they are suitable for (partially) servinge the request range
	 *     If so, assign tasks to those executors
	 * (3) If no running executor is suitable then add the request to the 'pending queue'
	 * 
	 * If the executor service
	 * 
	 * 
	 * @param requestRange
	 */
	public RequestIterator<T> request(Range<Long> requestRange) {

		RequestIterator<T> result = new RequestIterator<>(this);

		return result;
//		
//		
//		RangeRequestContext<Long> cxt = new RangeRequestContext<Long>(requestRange);
//
//		// Claim existing pages in the given range and register a listener
//		// that auto-claims any pages that become available
//		synchronized (pageCache) {
//			RangeMap<Long, Ref<RangeBuffer<T>>> claims = pageCache.claimAll(requestRange);
//			
//			RangeSet<Long> ranges = claims.asMapOfRanges().keySet();
//			RangeSet<Long> gaps = RangeUtils.gaps(ranges, requestRange);
//			
//			if (!gaps.isEmpty()) {
//				// Check whether there are already executor contexts to which the requests can be added
//				
//				
//				
//				
//			}
//		}

		
	}
	
	
	public Flowable<T> apply(Range<Long> range) {
		
		return Flowable.generate(
			() -> request(range),
			(it, e) -> {
				if (it.hasNext()) {
					T item = it.next();
					e.onNext(item);
				} else {
					e.onComplete();
				}				
			},
			RequestIterator::abort);
	}
	
	
	public static <V> ListPaginator<V> wrap(ListPaginator<V> backend) {
		return new SmartRangeCacheImpl<V>(backend);
	}

	@Override
	public Single<Range<Long>> fetchCount(Long itemLimit, Long rowLimit) {
		// If the size is known then return it - otherwise send at most one query to the backend for counting
		// and return the result.
		// Extension: If the count becomes known before the query returns then we could abort the count-request
		// and return the newly known value instead
		throw new UnsupportedOperationException("not implemented yet");
//		if (knownSize < 0) {
//			return
//		} else {
//			back
//		}
		
	}

}
