package org.aksw.commons.rx.cache.range;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

/**
 * A list where ranges can be marked as 'loaded'.
 *
 * Access to non-loaded items block until they become loaded.
 * Producers are not managed by this class and must therefore be managed
 * externally.
 * 
 * Changes to the set of loaded ranges or the known size synchronize on 'this'.
 * 
 * 
 * @author raven
 *
 * @param <T>
 */
public class RangeBuffer<T> {
	protected T[] buffer;
	
	protected List<T> backend = Arrays.asList(buffer);
	
	/**
	 * If the value is null then the range is considered as successfully loaded.
	 * If a throwable is present then there was an error processing the range
	 */
	protected RangeMap<Integer, Throwable> loadedRanges;
	protected volatile int knownSize;

	protected ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	
	public T[] getBuffer() {
		return buffer;
	}
	
	public List<T> getBufferAsList() {
		return backend;
	}
	
	@SuppressWarnings("unchecked")
	public RangeBuffer(int size) {
		this((T[])new Object[size], TreeRangeMap.create(), -1);
	}

	public RangeBuffer(T[] buffer, RangeMap<Integer, Throwable> loadedRanges, int knownSize) {
		super();
		this.buffer = buffer;
		this.loadedRanges = loadedRanges;
		this.knownSize = knownSize;
	}
	
	public ReadWriteLock getReadWriteLock() {
		return readWriteLock;
	}
	
	public Iterator<T> get(int offset) {
		return new RangeBufferIterator<>(this, offset);
	}
	
	public int getKnownSize() {
		return knownSize;
	}
	
	public RangeMap<Integer, Throwable> getLoadedRanges() {
		return loadedRanges;
	}

	
	public int getCapacity() {
		return backend.size();
	}

//	public void add(T item) {
//		int idx = numLoadedItems.getAndIncrement();
//		items[idx] = item;
//		notifyAll();
//	}

	public void put(int offset, Object arrayWithItemsOfTypeT) {
		put(offset, arrayWithItemsOfTypeT, 0, Array.getLength(arrayWithItemsOfTypeT));
	}
	
	public void put(int pageOffset, Object arrayWithItemsOfTypeT, int arrOffset, int arrLength) {
		Lock writeLock = readWriteLock.writeLock();
		writeLock.lock();

		try {
			System.arraycopy(arrayWithItemsOfTypeT, arrOffset, buffer, pageOffset, arrLength);
			loadedRanges.put(Range.closedOpen(pageOffset, pageOffset + arrLength), null);
		} finally {
			writeLock.unlock();
		}
	}
	
	/** Sets the known size thereby synchronizing on 'this' */
	public void setKnownSize(int size) {
		if (knownSize < 0) {
			Lock writeLock = readWriteLock.writeLock();
			writeLock.lock();
			try {
				this.knownSize = size;
			} finally {
				writeLock.unlock();
			}
		}
	}

	/** -1 if unknown */
	public int knownSize() {
		return knownSize;
	}
	
//	public static <T> Page<T> create(int pageSize) {
//		return new Page<T>((T[])new Object[pageSize], 0);
//	}
}
