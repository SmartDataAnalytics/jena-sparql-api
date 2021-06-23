package org.aksw.jena_sparql_api.rx;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.jsonldjava.shaded.com.google.common.base.Predicate;


/**
 * This class is a wrapper for a callable. The wrapper introduces an abort method
 * that specifically only interrupts the invocation of the callable.
 * Should the callable return before abort is called then the thread that invoked the callable
 * is not interrupted.
 *
 * The delegate can be exchanged but this must not happen concurrently.
 *
 * @author raven
 *
 */
public class CallableWithAbortFactory
{
    protected volatile Thread thread;
    protected volatile boolean isDone = true;
    protected AtomicBoolean abortRequested = new AtomicBoolean();

    protected Predicate<? super Throwable> isAbortException;

    public CallableWithAbortFactory(Predicate<? super Throwable> isAbortException) {
        super();
        this.isAbortException = isAbortException;
    }


    public boolean isDone() {
        return isDone;
    }


    public void abort() {
        boolean alreadyAbortRequested = abortRequested.getAndSet(true);
//        System.out.println(String.format("Abort requested redundant=%s --- isDone=%s", alreadyAbortRequested, isDone));

        if (!alreadyAbortRequested) {
            if (!isDone) {
                synchronized (this) {
                    if (!isDone) {
                        if (thread != null) {
//                            System.out.println("INTERRUPTING");
                            thread.interrupt();
                        }
                    }
                }
            }
        }
    }

    public <T> CallableWithAbort<T> setDelegate(Callable<T> delegate) {
        if (abortRequested.get()) {
            throw new IllegalStateException("Cannot set a new delegate after abort was requested");
        }

        if (!isDone) {
            throw new IllegalStateException("Cannot set a new delegate if the prior one has not completed execution");
        }

        return new CallableWithAbortImpl<>(delegate);
    }

    public class CallableWithAbortImpl<T>
        implements CallableWithAbort<T>
    {
        public CallableWithAbortImpl(Callable<T> delegate) {
            super();
            this.delegate = delegate;
        }

        protected Callable<T> delegate;

        @Override
        public T call() throws Exception {
            T result = null;
            thread = Thread.currentThread();

            if (Thread.interrupted() || abortRequested.get()) {
                isDone = true;
                throw new CancellationException();
            } else {
                try {
                    // Capture the case where there was a concurrent abort request
                    // while isDone was still true
                    isDone = false;
                    if (!abortRequested.get()) {
                        result = delegate.call();
                    }
                } catch (Exception e) {
                    boolean isAborted = isAbortException.test(e);

                    if (isAborted) {
//                        System.out.println("Hard Cancelled!");
                        throw new CancellationException();
                    } else {
//                        System.out.println("Exception " + isDone);
                        throw new RuntimeException(e);
                    }
                } finally {
                    isDone = true;

                    // If abort was requested we may need to clear the interrupted flag
                    if (abortRequested.get()) {
                        synchronized (this) {
                            boolean wasInterrupted = Thread.interrupted();
//                            System.out.println("wasInterrupted: " + wasInterrupted);
                        }
                    }
                }
            }

            return result;
        }

        @Override
        public void abort() {
            CallableWithAbortFactory.this.abort();
        }
    }
}
