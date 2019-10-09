package org.aksw.jena_sparql_api.io.endpoint;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;


/**
 * A wrapper for a channel that blocks upon reaching the end of its channel
 * and waits for a notification from a reference channel
 * 
 * When there is no more data, the reader by default waits for a notify on referenceChannel.
 * Alternatively, a poll interval can be specified, causing the reader to sleep for the
 * given amount of time
 * 
 * The reference channel may be a channel that actually written to
 * by the JVM, but it may also be just a 'dummy' channel that represents a write process
 * outside of the JVM (such as by a system process).
 * 
 * @author raven
 *
 */
public class ConcurrentFileReader
	implements ReadableByteChannel
{
	protected Channel referenceChannel;
	protected SeekableByteChannel currentReadChannel;
	protected Integer pollIntervalInMs;

	public static ConcurrentFileReader create(Path path, Channel writeChannel, Integer pollIntervalInMs) throws IOException {
		SeekableByteChannel currentReadChannel = Files.newByteChannel(path, StandardOpenOption.READ);
		ConcurrentFileReader result = new ConcurrentFileReader(writeChannel, currentReadChannel, pollIntervalInMs);
		return result;
	}

	public ConcurrentFileReader(Channel writeChannel, SeekableByteChannel currentReadChannel, Integer pollIntervalInMs) {
		this.referenceChannel = writeChannel;
		this.currentReadChannel = currentReadChannel;
		this.pollIntervalInMs = pollIntervalInMs;
	}

	@Override
	public boolean isOpen() {
		boolean result = currentReadChannel != null || currentReadChannel.isOpen();
		return result;
	}
	
	public boolean isAborted() {
		boolean result = referenceChannel instanceof ConcurrentFileEndpoint
				? ((ConcurrentFileEndpoint)referenceChannel).isAborted()
				: false;
			
		return result;
	}

	@Override
	public void close() throws IOException {
		currentReadChannel.close();
	}

	
	/**
	 * Read method that blocks if no data is available
	 * but the write channel is still open
	 */
	@Override
	public int read(ByteBuffer dst) throws IOException {
		int result;
		while(true) {
			if(isAborted()) {
				throw new IOException("The underlying stream has been aborted");
			}
			
			result = currentReadChannel.read(dst);
			if(result == -1) {
				if(referenceChannel.isOpen()) {
					try {
						synchronized (referenceChannel) {
							// Recheck open state is case it was closed before
							// entering the synchronized block
							if(referenceChannel.isOpen()) {
								System.out.println("Thread #" + Thread.currentThread().getId() + ": Reader going to sleep");
								if(pollIntervalInMs == null) { 
									referenceChannel.wait();
								} else {
									Thread.sleep(pollIntervalInMs);
								}
								System.out.println("Thread #" + Thread.currentThread().getId() + ": Reader awoke");
							}
						}
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				} else {
					break;
				}
			} else {
				break;
			}
		}

		return result;
	}

}