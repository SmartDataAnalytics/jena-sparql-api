package org.aksw.commons.util.sink;

public interface Sink<T>
	extends AutoCloseable
{
	void accept(T item);
	void flush();

	@Override
	void close();
}
