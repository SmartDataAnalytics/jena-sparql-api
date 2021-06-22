package org.aksw.jena_sparql_api.rx;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.concurrent.CancellationException;

import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.RiotParseException;

/**
 * Helper class used in {@link RDFDataMgrEx} to track resources involved in RDF parsing
 *
 *
 * @author raven
 *
 * @param <I> InputStream type
 * @param <T> Item type of the resulting flow, typically Triples or Quads
 */
public class FlowState<T> {
    protected InputStream in;
    protected Thread producerThread;
    protected Throwable raisedException;
    protected Iterator<T> iterator;
    protected volatile boolean closeInvoked;
    // protected Thread consumerThread;

    protected CallableWithAbortFactory consumerInterrupter =
            new CallableWithAbortFactory(t -> t instanceof CancellationException);


    // Only default ctor - attributes are set one after the other at different places



    public Thread getProducerThread() {
        return producerThread;
    }

    public void setProducerThread(Thread thread) {
        producerThread = thread;
    }

    public void setIn(InputStream in) {
        this.in = in;
    }

    public void setIterator(Iterator<T> iterator) {
        this.iterator = iterator;
    }

    public void handleProducerException(Thread thread, Throwable e) {
        boolean report = true;

        // Abort the consumer who may be still be waiting for data from the deceased producer
        consumerInterrupter.abort();

        // If close was invoked, skip exceptions related to the underlying
        // input stream having been prematurely closed
        if (closeInvoked) {
            if(e instanceof RiotException) {
                String msg = e.getMessage();
                if(msg.equalsIgnoreCase("Pipe closed") || msg.equals("Consumer Dead")) {
                    report = false;
                }
            }
        }

        if (report) {
            if (raisedException == null) {
                raisedException = e;
            }
            // If we receive any reportable exception after the flowable
            // was closed, raise them so they don't get unnoticed!
            if (closeInvoked && !(e instanceof RiotParseException)) {
                throw new RuntimeException(e);
            }
        }
    }

    public void close() throws IOException {
        closeInvoked = true;
        // The producer thread may be blocked because not enough items were consumed
//				if(thread[0] != null) {
//					while(thread[0].isAlive()) {
//						thread[0].interrupt();
//					}
//				}

        // We need to wait if iterator.next is waiting
//				synchronized(this) {
//
//				}

        // Try to close the iterator 'it'
        // Otherwise, forcefully close the stream
        // (may cause a (usually/hopefully) harmless exception)
        try {
            if(iterator instanceof Closeable) {
                ((Closeable)iterator).close();
            } else if (iterator instanceof org.apache.jena.atlas.lib.Closeable) {
                ((org.apache.jena.atlas.lib.Closeable)iterator).close();
            }
        } finally {
            try {
                in.close();
            } finally {
                try {
                    // Consume any remaining items in the iterator to prevent blocking issues
                    // For example, Jena's producer thread can get blocked
                    // when parsed items are not consumed
//							System.out.println("Consuming rest");
                    // FIXME Do we still need to consume the iterator if we
                    // interrupt the producer thread - or might that lead to triples / quads
                    // getting lost?
//							Iterators.size(it);
                } catch(Exception e) {
                    // Ignore silently
                } finally {


//                    if (consumerThread != null) {
//                        consumerThread.interrupt();
//                    }

                    // The generator corresponds to the 2nd argument of Flowable.generate
                    // The producer may be blocked by attempting to put new items on a already full blocking queue
                    // The consumer in it.hasNext() may by waiting for a response from the producer
                    // So we interrupt the producer to death

                    interruptUntilDead(producerThread);
                    consumerInterrupter.abort();
                }
            }
        }
    }

    public static void interruptUntilDead(Thread thread) {
        if (thread != null) {
            while (thread.isAlive()) {
                thread.interrupt();

                try {
                    thread.join(1000);
                } catch(InterruptedException e) {
                }
            }
        }
    }
}