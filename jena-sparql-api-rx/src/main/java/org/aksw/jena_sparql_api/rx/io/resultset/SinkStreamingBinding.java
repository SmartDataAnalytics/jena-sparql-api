package org.aksw.jena_sparql_api.rx.io.resultset;

import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.aksw.commons.rx.util.RxUtils;
import org.aksw.jena_sparql_api.rx.ResultSetRxImpl;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.ResultSetMgr;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingMap;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.subjects.PublishSubject;

/**
 * A sink for writing out bindings as a result set using {@link ResultSetMgr}
 *
 * Internally uses a ResultSet that is backed by a Flowable&lt;Binding&gt;.
 * Calling send(binding) publishes to that flowable and thus makes a new binding available in the result set.
 *
 * As the ResultSet blocks until items become available the ResultSetWriter runs in a another thread
 *
 * Uses a {@link PublishSubject} wrapped with ugly synchronization
 *
 * @author raven
 *
 */
public class SinkStreamingBinding
    extends SinkStreamingBase<Binding>
{
    protected OutputStream out;
    protected List<Var> resultVars;
    protected Lang lang;

    protected BlockingQueue<Binding> blockingQueue = new ArrayBlockingQueue<Binding>(Flowable.bufferSize());

    protected Thread thread = null;
    protected Throwable threadException = null;
    protected boolean closed = false;

    public static final Binding POISON = BindingFactory.create();

    public SinkStreamingBinding(OutputStream out, List<Var> resultVars, Lang lang) {
        super();
        this.out = out;
        this.resultVars = resultVars;
        this.lang = lang;
    }

    @Override
    public void flush() {
        IO.flush(out);
    }

    protected void checkThread() {
        if (threadException != null) {
            throw new IllegalStateException("Consumer thread terminated exceptionally", threadException);
        } else if (!thread.isAlive()) {
            throw new IllegalStateException("Consumer thread already terminated (without exception)");
        }
    }

    @Override
    public void close() {
        if (thread != null) {

            boolean closedIncompleteResult = false;
            if (thread.isAlive()) {
                closedIncompleteResult = !blockingQueue.isEmpty();
//                if (closedIncompleteResult) {
                    if (!blockingQueue.contains(POISON)) {
                        try {
                            blockingQueue.put(POISON);
                        } catch (InterruptedException e) {
                            throw new RuntimeException("Failed to put poison into queue");
                        }
                    }
//                }
            }

            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (threadException != null) {
                throw new RuntimeException("Consumer thread terminated exceptionally", threadException);
            }

            if (closedIncompleteResult) {
                throw new RuntimeException("Closed incomplete result set (finish() was not called)");
            }
        }
    }

    @Override
    protected void startActual() {
        Flowable<Binding> flowable = RxUtils.fromBlockingQueue(blockingQueue, item -> item == POISON);
        thread = new Thread(() -> {
            try(QueryExecution qe = new ResultSetRxImpl(resultVars, flowable).asQueryExecution()) {
                ResultSet resultSet = qe.execSelect();
                ResultSetMgr.write(out, resultSet, lang);
            }
        });
        thread.setUncaughtExceptionHandler((t, e) -> threadException = e);
        thread.start();
    }

    @Override
    protected void sendActual(Binding item) {
    	// Create a copy the bindings so we don't run into race
    	// conditions when we process them in another thread.
    	// For instance, accessing TDB2 bindings after
    	// the underlying query execution has been concurrently closed
    	// raises an exception
        BindingMap copy = BindingFactory.create();
        copy.addAll(item);

        checkThread();
        try {
            blockingQueue.put(copy);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void finishActual() {
        checkThread();
        try {
            blockingQueue.put(POISON);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
