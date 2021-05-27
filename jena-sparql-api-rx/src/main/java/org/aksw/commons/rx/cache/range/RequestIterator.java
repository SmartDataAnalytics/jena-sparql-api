package org.aksw.commons.rx.cache.range;

import java.util.concurrent.ConcurrentNavigableMap;

import org.aksw.commons.util.ref.Ref;
import org.aksw.jena_sparql_api.utils.RangeUtils;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;

/**
 * The class drives the iteration of items from the cache
 * and triggers fetching of data as necessary.
 * 
 * Thereby this class does not fetch the data directly, but it declares
 * interest in data ranges. The SmartRangeCache will schedule loading of the region
 * at least as long as interest is expressed.
 * 
 * @author raven
 *
 * @param <T>
 */
public class RequestIterator<T>
	extends AbstractIterator<T>
{
	protected SmartRangeCacheImpl<T> cache;

	/**
	 * The original request range by this request.
	 * In general, the original request range has to be broken down into smaller ranges
	 * because of result size limits of the backend
	 */
	protected Range<Long> requestRange;
	

	/** Pages claimed so far by this iterator;
	 * new pages will be added to this map asynchronously when they become available */
	protected ConcurrentNavigableMap<Long, Ref<RangeBuffer<T>>> claimedPages;
	
	
	protected Runnable abortAction = null;
	protected boolean isAborted = false;
	
	/** The index of the next item to read */
    protected long currentOffset;
	
	protected long claimAheadLength;
	
	/**
	 * The claim ahead range starts at the current offset and has length claimAheadLimit
	 */
	protected Range<Long> getClaimAheadRange() {
		// TODO Use -1 or Long.MAX_VALUE for unbounded case?
		return Range.closedOpen(currentOffset, currentOffset + claimAheadLength);
	}
	
	
	protected void onPageLoaded(long offset, Ref<RangeBuffer<T>> content) {
		Range<Long> claimAheadRange = getClaimAheadRange();
		
		if (claimAheadRange.contains(offset)) {
			claimedPages.put(offset, content.acquire());
		}
	}
	
	protected void init() {
//		RangeCache<Long, RangeBuffer<T>> pageCache = cache.pageCache;
//		synchronized (pageCache) {
//			RangeMap<Long, Ref<RangeBuffer<T>>> claims = pageCache.claimAll(requestRange);
			
//			RangeSet<Long> ranges = claims.asMapOfRanges().keySet();
//			RangeSet<Long> gaps = RangeUtils.gaps(ranges, requestRange);
			
			
//		}
		
		// cache.activeRequests.add(this);
		synchronized (this) {
			if (!isAborted) {
				abortAction = cache.register(this);
			}
		}
	}
	
	@Override
	protected T computeNext() {
		
		// Get the page at offset, then return an iterator over the page's items
		// That iterator will block if items have not yet been loadded
		// Once all items of a page have been iterated, release the page and increment
		// the offset
		
		long nextGapOffset = 0;
		
		
		return null;
	}

	/**
	 * Abort the request
	 */
	public void abort() {
		// Prevent creating an action after this method is called
		if (!isAborted) {
			synchronized (this) {
				if (!isAborted) {
					abortAction.run();
					isAborted = true;
				}
			}
		}
	}

	
}
