package org.aksw.jena_sparql_api.io.binseach;

import java.nio.ByteBuffer;

/**
 * Interface for mapping a position to a corresponding byte buffer
 * 
 * @author raven
 *
 */
public interface ByteBufferSupplier {
	/**
	 * 
	 * @param position
	 * @param sizeHint
	 * @return
	 */
	//ByteBuffer get(long offset, int length);
	ByteBuffer get(long offset);
	long getSize();
}
