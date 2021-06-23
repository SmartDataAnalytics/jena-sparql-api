package org.aksw.commons.util.sink;


/**
 * A sink that collects items in a buffer.
 * Only when the buffer becomes full the items are flushed to the delegate in bulk.
 * Closing does not flush pending items!
 * 
 * This class is not thread-safe.
 * 
 * @author raven
 *
 * @param <T>
 */
public class BulkingSink<T>
	implements Sink<T>
{
	protected T[] buffer;
	protected BulkConsumer delegate;

	protected int nextOffsetInBuffer;
	
	protected boolean isClosed = false;
	
	protected void ensureOpen() {
		if (isClosed) {
			throw new IllegalStateException("Sink is already closed");
		}
	}
	
	@SuppressWarnings("unchecked")
	public BulkingSink(int bulkSize, BulkConsumer delegate) {
		super();
		
		if (bulkSize <= 0) {
			throw new IllegalArgumentException("Bulk size must be greater than 0");
		}
		
		this.buffer = (T[])new Object[bulkSize];
		this.delegate = delegate;
		
	}

	@Override
	public void accept(T item) {
		ensureOpen();
		
		buffer[nextOffsetInBuffer++] = item;
		
		if (nextOffsetInBuffer == buffer.length) {
			flushActual();
		}
	}

	@Override
	public void flush() {
		ensureOpen();
		flushActual();
	}
	
	protected void flushActual() {
		delegate.accept(buffer, 0, nextOffsetInBuffer);
		nextOffsetInBuffer = 0;
	}

	@Override
	public void close() {
		isClosed = true;
	}
}
