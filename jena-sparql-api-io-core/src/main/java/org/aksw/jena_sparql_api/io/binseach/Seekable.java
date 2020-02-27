package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * Interface for purely relative navigation along data which allows for navigating along
 * data of unknown or infinite size.
 * 
 * 
 * @author raven
 *
 */
public interface Seekable
	extends ReadableByteChannel
{
	Seekable clone();
	
	/**
	 * Optional operation.
	 * Get the position in this seekable
	 * 
	 * @return
	 */
	long getPos(); //throws IOException;

	/**
	 * Optional operation.
	 * Get the position in this seekable
	 * 
	 * @return
	 */
	void setPos(long pos); //throws IOException;

	
	/**
	 * Optional operation.
	 * Move one unit before the start of the seekable; raises an exception on infinite seekables
	 * 
	 * @return
	 */
	void posToStart() throws IOException;

	/**
	 * Optional operation.
	 * Move to one unit beyond the end of the seekable; raises an exception on infinite seekables
	 * 
	 * @return
	 */
	void posToEnd() throws IOException;

	
	byte get() throws IOException;
	
	/**
	 * The state of a seekable may be one unit before the start.
	 * In this state, if the seekable is non-empty, nextPos(1) must be a valid position
	 * 
	 * @return
	 */
	boolean isPosBeforeStart() throws IOException;
	
	/**
	 * The state of a seekable may be one unit beyond the end.
	 * In this state, if the seekable is non-empty, prevPos(1) must be a valid position
	 * @return
	 */
	boolean isPosAfterEnd() throws IOException;
	
	/**
	 * Attempt to advance the position by the given number of bytes.
	 * 
	 * Argument must not be negative.
	 *
	 * @param len
	 * @return True if the position was changed by the requested amount of bytes. False means that the position was unchanged. 
	 */
	boolean nextPos(int len) throws IOException;
	
	/**
	 * Attempt to step back the position by the given number of bytes.
	 * Argument must not be negative.
	 *
	 * @param len
	 * @return True if the position was changed by the requested amount of bytes. False means that the position was unchanged. 
	 */
	boolean prevPos(int len) throws IOException;

	/**
	 * Attempt to read bytes at the current position without altering the position
	 * 
	 * @param dst
	 * @param offset
	 * @param len
	 * @return
	 * @throws IOException
	 */
	default int peekNextBytes(byte[] dst, int offset, int len) throws IOException {
		int n = 0;
		if(!isPosAfterEnd()) {
			for(; n < len; ++n) {
				byte c = get();
				dst[offset + n] = c;
				if(!nextPos(1)) {
					break;
				}
			}
		}
		prevPos(n);
		return n;
	}

	
	/**
	 * Move the position to the next delimiter if it exists,
	 * or one element past the end of data such that isPosAfterEnd() yields true.
	 * Position is unchanged if already at delimiter
	 * 
	 * @param delimiter
	 * @return true if the position was changed, false otherwise
	 * @throws IOException
	 */
	default boolean posToNext(byte delimiter) throws IOException {
		while(get() != delimiter) {
			if(!nextPos(1)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Move the position to the previous delimiter if it exists,
	 * or one element past the end of data such that isPosBeforeStart() yields true.
	 * Position is unchanged if already at delimiter
	 * 
	 * 
	 * @param delimiter
	 * @return true if the position was changed, false otherwise
	 * @throws IOException
	 */
	default boolean posToPrev(byte delimiter) throws IOException {
		while(get() != delimiter) {
			if(!prevPos(1)) {
				return false;
			}
		}
		return true;
	}

	
//	boolean nextPos() throws IOException;
//	boolean prevPos() throws IOException;
	
	@Override
	default int read(ByteBuffer dst) throws IOException {
		int n = 0;
		if(!isPosAfterEnd()) {
			while(dst.remaining() > 0) {
				byte c = get();
				dst.put(c);
				++n;
				if(!nextPos(1)) {
					break;
				}
			}
		}
		
		return n;
	}
}
