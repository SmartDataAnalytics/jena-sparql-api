package org.aksw.commons.rx.cache.range;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentNavigableMap;

import org.aksw.commons.collections.PrefetchIterator;
import org.aksw.commons.util.ref.Ref;
import org.aksw.jena_sparql_api.utils.RangeUtils;
import org.apache.jena.ext.com.google.common.collect.Iterators;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Range;
import com.google.common.math.LongMath;

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

	
	/** Do not send requests to the backend as long as that many items can be served from the cache */
	protected long readAheadItemCount = 100;

		
	/** Pages claimed so far by this iterator;
	 * new pages will be added to this map asynchronously when they become available */
	protected ConcurrentNavigableMap<Long, Ref<RangeBuffer<T>>> claimedPages;
	
	
	protected Runnable abortAction = null;
	protected boolean isAborted = false;
	
	/** The index of the next item to read */
    protected long currentOffset;
	
	protected long claimAheadLength;
	
	
	
	/**
	 * In order to deal with large or infinite request ranges, request processed in blocks:
	 *  - Only pages within the block range are claimed. 
	 *  - Upon reading a percentage of the block a checkpoint is made that prepares the next block
	 *  - Data fetching is scheduled for any gaps in the block
	 */
	protected long blockLength;
	
	/** At a checkpoint the data fetching tasks for the next blocks are scheduled
	  */
	protected long nextCheckpointOffset = 0;
	
	
	public RequestIterator(SmartRangeCacheImpl<T> cache) {
		super();
		this.cache = cache;
	}
	
	
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
	
	
	/**
	 * Check whether there are any gaps ahead that require
	 * scheduling requests to the backend
	 * 
	 */
	public void checkpoint() throws Exception {

		List<RangeRequestExecutor<T>> candExecutors = new ArrayList<>();
		
		Range<Long> readAheadRange = null;
		
		List<CompletableFuture<Runnable>> pauseFutures = new ArrayList<>();

		// Prevent creation of new executors while we analyze the state
		cache.getExecutorCreationReadLock().lock();
		
		
		// Pause relevant running executors - executors are relevant if their working range
		// overlaps with the read ahead range of this iterator 
		for (RangeRequestExecutor<T> executor : cache.getExecutors()) {
			Range<Long> workingRange = executor.getWorkingRange();			
			if (!readAheadRange.intersection(workingRange).isEmpty()) {
				// Pause only returns when the executor has halted
				CompletableFuture<Runnable> pauseLock = executor.pause()
						.whenComplete((unpause, throwable) -> {
							Range<Long> pausedWorkingRange = executor.getWorkingRange();
							if (!readAheadRange.intersection(pausedWorkingRange).isEmpty()) {
								candExecutors.add(executor);
							}
						});
			}	
		}
		
		
		
		
		// Wait for all relevant executors to pause
		CompletableFuture<Void> allPaused = CompletableFuture.allOf(pauseFutures.toArray(new CompletableFuture[0]));		
		allPaused.get();
		
		
		// Check the claimed pages ahead of the current offset for any gaps
		long pageId = cache.getPageIdForOffset(currentOffset);
		
		long pageSize = cache.getPageSize();
		long pageOffset = pageId * pageSize;
		
		long readAheadPages = readAheadItemCount / pageSize;
		
		
		long firstGapSeen = Long.MAX_VALUE;

		// Create an iterator over the gaps
		Iterator<Range<Long>> gaps = new PrefetchIterator<Range<Long>>() {
			int i = 0;
			@Override
			protected Iterator<Range<Long>> prefetch() throws Exception {
				long pageOffset = (pageId + i) * pageSize;

				Iterator<Range<Long>> r;
				// If a page is not loaded at all then a request has to start at that page
				if (!claimedPages.containsKey(pageOffset)) {
					r = Iterators.singletonIterator(Range.closedOpen(pageOffset, pageOffset + pageSize));					
				} else {
					Ref<RangeBuffer<T>> pageRef = claimedPages.get(pageOffset);
					// Shift the range to the page offset
					r = Iterators.transform(pageRef.get().getLoadedRanges().asMapOfRanges().keySet().iterator(),
							range -> RangeUtils.apply(range, pageOffset, (endpoint, value) -> LongMath.saturatedAdd(endpoint, (long)value)));
				}
				return r;
			}
		};


		// (1) If there is not gap within the read ahead range then locate the first gap
		// within the effective claim ahead range and schedule a checkpoint at gap.offset - read ahead range
		
		// (2) If there is a gap within the read ahead range then it is necessary to determine the request
		// range. The request range starts from the offset of the first gap.
		
		// (2a) If the request range is within the claim ahead range then set the request range
		//      at most to the endpoint of the last gap in that range
		//      More specifically, start covering the gaps and decide whether to schedule checkpoints
		//      or requests.
		
		// (2b) Note that the request range may extend over claim ahead - hence, 
		//      In that case we are forced to create a new executor with the request limit
	    //      because we cannot guarantee that pages will be available
		//      
		
		// candExecutors.iterator().next().requestEndpoint(this, firstGapSeen);
		
		
		
		while (gaps.hasNext()) {
			Range<Long> gap = gaps.next();
			
			// Abort if the gap offset is outside of the working range
			
			long gapStart = pageOffset + gap.lowerEndpoint();
			long gapEnd = pageOffset + gap.upperEndpoint();
			
			// executor.tryExtendRange(gapEnd);
			
			// Remove candidate executors that cannot serve the gap
			// In the worst case no executors remain - in that case
			// we have to create a new executor that starts after the last
			// position that can be served by one of the current executors
			List<RangeRequestExecutor<T>> nextCandExecutors = new ArrayList<>(candExecutors.size());
			for (RangeRequestExecutor<T> candExecutor : candExecutors) {
				Range<Long> ewr = candExecutor.getWorkingRange();
				if (ewr.encloses(gap)) {
					nextCandExecutors.add(candExecutor);
				}
			}

			// No executor could deal with all the gaps in the read ahead range
			// Find the executor that can 
			if (nextCandExecutors.isEmpty()) {
				break;
			}
		}
		
		
		
		
		
		// for every gap decide on the following
		// (.) can be scheduled for an executor?
		// (.) if not, is a new executor needed immediately (gap within read ahead range)?
		// (.) do we need to defer the decision and schedule another checkpoint?
		
		// Pass as many gaps as possible in the read ahead range to a running executor
		// If there is any unserved gap in the rar then start a new executor
		

		
			
//			currentOffset
		
		
		// Unpause the paused executors
		for (CompletableFuture<Runnable> future : pauseFutures) {
			future.get().run();
		}
	}
	
	
	@Override
	protected T computeNext() {
		if (currentOffset == nextCheckpointOffset) {
			try {
				checkpoint();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		RangeBuffer<T> currentPage = null;
		
		
		
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
					isAborted = true;
					close();
				}
			}
		}
	}

	
	public void close() {
		// TODO Release all claimed pages
		// TODO Release all claimed task-ranges
	}
	
}
