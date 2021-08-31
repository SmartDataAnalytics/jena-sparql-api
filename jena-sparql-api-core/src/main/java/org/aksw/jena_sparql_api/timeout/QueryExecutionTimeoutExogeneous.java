package org.aksw.jena_sparql_api.timeout;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.arq.core.query.QueryExecutionDecoratorBase;
import org.aksw.jena_sparql_api.delay.extra.Delayer;
import org.aksw.jena_sparql_api.delay.extra.DelayerDefault;
import org.apache.jena.query.QueryExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Query execution that applies a timeout "from the outside" (= exogeneous).
 * Upon query execution, a new thread is created that calls .abort() on the
 * underlying query execution once the timeout is reached.
 *
 * @author Claus Stadler, Oct 9, 2018
 *
 */
public class QueryExecutionTimeoutExogeneous
    extends QueryExecutionDecoratorBase<QueryExecution>
{
    private static final Logger logger = LoggerFactory.getLogger(QueryExecutionTimeoutExogeneous.class);


    protected Duration duration1;
    protected Duration duration2;

    protected Thread thread;

    protected boolean reachedTimeout = false;

    public QueryExecutionTimeoutExogeneous(QueryExecution decoratee) {
        super(decoratee);
    }

    @Override
    protected void beforeExec() {
//		System.out.println("Setting up thread");
        Delayer delayer = DelayerDefault.createFromNow(duration1.toMillis());

        thread = new Thread() {
            @Override
            public void run() {
                try {
                    if(!isInterrupted()) {
                        synchronized(thread) {
                            if(!isInterrupted()) {
//								System.out.println("doing delay " + delayer);
                                delayer.doDelay();
                                // Once the delay is reached, abort
//								System.out.println("Delay reached");
                                try {
                                    abort();
                                } catch(Exception e) {
                                    logger.warn("Exception while aborting query", e);
                                }

                                // Set the interrupt flag
                                interrupt();
//								throw new QueryCancelledException();
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    // Nothing to do
//					throw new RuntimeException(e);
                }
//				} catch(QueryCancelledException e) {
//					throw new
//				}
            }
        };
        thread.start();
    }

    @Override
    protected synchronized void afterExec() {
//		System.out.println("Interrupting...");
        if(!thread.isInterrupted()) {
            synchronized (thread) {
                thread.interrupt();
            }
        }
    }


    @Override
    public void setTimeout(long timeout) {
        duration1 = Duration.ofMillis(timeout);
        duration2 = duration1;
    }

    @Override
    public void setTimeout(long timeout, TimeUnit timeoutUnits) {
        duration1 = Duration.ofMillis(timeoutUnits.toMillis(timeout));
        duration2 = duration1;
    }

    @Override
    public void setTimeout(long timeout1, long timeout2) {
        duration1 = Duration.ofMillis(timeout1);
        duration2 = Duration.ofMillis(timeout2);
    }

    @Override
    public void setTimeout(long timeout1, TimeUnit timeUnit1, long timeout2, TimeUnit timeUnit2) {
        duration1 = Duration.ofMillis(timeUnit1.toMillis(timeout1));
        duration2 = Duration.ofMillis(timeUnit2.toMillis(timeout2));
    }
}
