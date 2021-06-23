package org.aksw.commons.rx.cache.range;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;

/**
 * An iterator over a range buffer that blocks if a items are not loaded.
 * 
 * @author raven
 *
 * @param <T>
 */
public class RangeBufferIterator<T>
	extends AbstractIterator<T>
{
	protected RangeBuffer<T> rangeBuffer;
	protected int currentIndex;
	
	/** Iterator over a range in the page starting at currentOffset */
	protected Iterator<T> rangeIterator = null;
	
	/** Number of items read from rangeIterator */
	protected int readsFromCurrentRange = 0;
	
	public RangeBufferIterator(RangeBuffer<T> page, int currentIndex) {
		super();
		this.rangeBuffer = page;
		this.currentIndex = currentIndex;
	}
	
	
	@Override
	protected T computeNext() {
		T result;
	
		RangeMap<Integer, Throwable> loadedRanges = rangeBuffer.getLoadedRanges();
	
		while (rangeIterator == null || !rangeIterator.hasNext()) {
			currentIndex += readsFromCurrentRange;
			readsFromCurrentRange = 0;
			
			Entry<Range<Integer>, Throwable> entry = null;	
			Lock lock = rangeBuffer.getReadWriteLock().readLock();
			lock.lock();
			try {
				// If the index is outside of the known size then abort
				int knownSize = rangeBuffer.getKnownSize();
				if (knownSize >= 0 && currentIndex >= knownSize) {
					return endOfData();
				} else {	
					entry = loadedRanges.getEntry(currentIndex);
					
					if (entry == null) {
						// Wait for data to become available
						try {
							wait();
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
					}
				}
			} finally {
				lock.unlock();
			}
			
			if (entry != null) {
				Throwable throwable = entry.getValue();
				if (throwable != null) {
					throw new RuntimeException("Attempt to read a range of data marked with an error", throwable);
				}
				
				Range<Integer> range = entry.getKey();
				rangeIterator = rangeBuffer.getBufferAsList()
						.subList(range.lowerEndpoint(), range.upperEndpoint())
						.iterator();
				break;
			}
		}
	
		result = rangeIterator.next();
		++readsFromCurrentRange;
		return result;
	}
}