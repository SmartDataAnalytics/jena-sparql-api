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

    public static boolean isRiotBrokenPipeException(Throwable t) {
        boolean result = false;
        if (t instanceof RiotException) {
            String msg = t.getMessage();
            if(msg.equalsIgnoreCase("Pipe closed") || msg.equals("Consumer Dead")) {
                result = true;
            }
        }

        return result;
    }

    public void handleProducerException(Thread thread, Throwable e) {

        boolean report = true; // May be set to false in the following

        // We no longer need to abort the consumer because it will be notified
        // with a poison
        // consumerInterrupter.abort();

        // If close was invoked, skip exceptions related to the underlying
        // input stream having been prematurely closed
        if (closeInvoked) {
            if (e instanceof RiotParseException
                    || e instanceof CancellationException
                    || isRiotBrokenPipeException(e)) {
                report = false;
            }
        }

        if (report) {
            if (raisedException == null) {
                raisedException = e;
            }
            // If we receive any reportable exception after the flowable
            // was closed then raise them so they don't get unnoticed!
            if (closeInvoked) {
                throw new RuntimeException(e);
            }
        }
    }

    public void close() throws IOException {
        closeInvoked = true;

        try {
            // Close the underlying input stream - this may cause producer threads to terminate
            in.close();
        } finally {
            try {
                // Close the iterator which may be a consumer to the producer
                if(iterator instanceof Closeable) {
                    ((Closeable)iterator).close();
                } else if (iterator instanceof org.apache.jena.atlas.lib.Closeable) {
                    ((org.apache.jena.atlas.lib.Closeable)iterator).close();
                }
            } finally {
                // The generator corresponds to the 2nd argument of Flowable.generate
                // The producer may be blocked by attempting to put new items on a already full blocking queue
                // The consumer in it.hasNext() may by waiting for a response from the producer
                // So we interrupt the producer to death
                interruptUntilDead(producerThread);

                // The code in RDFDataMgrRx is designed to notify the consumer
                // using the poison pill approach once the producer exits
                // Therefore the should be no more need to cancel any waiting of the consumer
                // consumerInterrupter.abort();
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