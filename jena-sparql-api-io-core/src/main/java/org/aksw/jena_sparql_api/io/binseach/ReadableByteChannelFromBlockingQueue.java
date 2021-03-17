package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// Not used anymore but kept for reference
public class ReadableByteChannelFromBlockingQueue
	implements ReadableByteChannel //, Subscriber<ByteBuffer>
{
	protected static final ByteBuffer POISON = ByteBuffer.wrap(new byte[0]);
	
	protected BlockingQueue<ByteBuffer> clientQueue = new LinkedBlockingQueue<>(32);
	protected ByteBuffer currentBuffer;
	
	
	protected Throwable abortException = null;
	
	protected boolean isOpen = true;
	protected Runnable closeAction;
	
	public ReadableByteChannelFromBlockingQueue() {
	    super();
	}
	
	public ReadableByteChannelFromBlockingQueue(Runnable closeAction) {
	    super();
	    this.closeAction = closeAction;
	}
	
	public boolean isComplete() {
	    return currentBuffer == POISON;
	}
	
	
	//@Override
	public void complete() {
//		System.out.println("OnComplete called");
		put(POISON);
	}
	
	//@Override
	public void put(ByteBuffer bodyBuffer) {
//		System.out.println("put");
        try {
    		clientQueue.put(bodyBuffer);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * If the stream is already closed, this is a noop.
	 *
	 * Otherwise, sets the abort exception and interrupts any wait for data
	 *
	 */
	//@Override
//	public void onError(Throwable t) {
//	    synchronized(this) {
//	        if() {
//	            abortException = t;
//	            notifyAll();
//	        }
//	    }
//	}
//	
	
	/**
	 * Supply data to the channel, which will be returned by read operations in sequence
	 *
	 * @param data
	 */
	//public void supplyData(ByteBuffer data) {
	//    clientQueue.add(data);
	//}
	
	
	
	// AsynchronousByteChannel methods
	
	@Override
	public void close() throws IOException {
	    if(closeAction != null) {
	        closeAction.run();
	    }
	    isOpen = false;
	}
	
	@Override
	public boolean isOpen() {
	    return isOpen;
	}
	
	
	@Override
	public  int read(ByteBuffer dst) {
//		System.out.println("read");
	    int result = 0;
	
	    int remaining = dst.remaining();
	    //int readBytes = 0;
	
	    while(remaining > 0) {
	        int available = currentBuffer == null ? 0 : currentBuffer.remaining();
	
	        if(available == 0) {
	        	if(currentBuffer == POISON) {
	        		result = -1;
	        		break;
	        	} else {
		        	try {
						currentBuffer = clientQueue.take();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
		        	
		        	if(currentBuffer == POISON) {
		        		// return whatever result we have so far
		        		break;
		        	}
	        	}
	        }
	
	        int toRead = Math.min(remaining, available);
	
	        int off = currentBuffer.position();
	        int newOff = off + toRead;
	
	        ByteBuffer tmp = currentBuffer.duplicate();
	        ((Buffer)tmp).limit(newOff);
	        dst.put(tmp);
	//        System.out.println("Got:");
	//        System.out.println(new String(dst.array(), StandardCharsets.UTF_8));
	
	        currentBuffer.position(newOff);
	
	        result += toRead;
	        remaining -= toRead;
	    }
		
	    return result;
	}

}