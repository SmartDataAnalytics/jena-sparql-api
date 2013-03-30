package org.aksw.jena_sparql_api.core;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QueryExecution;


/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 11/19/11
 *         Time: 10:17 PM
 *         <p/>
 *         A helper for timeouts on QueryExecutions:
 *         <p/>
 *         Setting maxExecution time will result in a call to
 *         queryExecution.abort() after the time limit is reached.
 *         <p/>
 *         Setting max retrievalTime will result in a call to
 *         queryExecution.close() after the time limit has been reached.
 */
public class QueryExecutionTimeoutHelper {
    private static final Logger logger = LoggerFactory.getLogger(QueryExecutionTimeoutHelper.class);

    // For now we assume that calls to "abort" and "close" do not block
    // and therefore a single Timer object is sufficient,
    private static Timer executionTimer = new Timer(true);
    private static Timer retrievalTimer = new Timer(true);


    private QueryExecution queryExecution;

    private Time maxExecutionTime = null;
    private Time maxRetrievalTime = null;

    private TimerTask executionTask = null;
    private TimerTask retrievalTask = null;

    public QueryExecutionTimeoutHelper(QueryExecution queryExecution) {
        this.queryExecution = queryExecution;
    }

    public synchronized void startExecutionTimer() {
        if (maxExecutionTime != null) {
            long delay = maxExecutionTime.getTimeUnit().toMillis(maxExecutionTime.getTime());

            executionTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        queryExecution.abort();
                    } catch (Exception e) {
                        logger.warn("Exception while aborting a QueryExecution that has reached its execution timeout", e);
                    }

                    //throw new RuntimeException("Query execution has reached its time limit.");
                }
            };

            executionTimer.schedule(executionTask, delay);
        }
    }

    public synchronized void stopExecutionTimer() {
        if (executionTask != null) {
            executionTask.cancel();
        }
    }

    public synchronized void startRetrieval() {
        if (maxRetrievalTime != null) {
            long delay = maxRetrievalTime.getTimeUnit().toMillis(maxRetrievalTime.getTime());

            retrievalTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        queryExecution.close();
                    } catch (Exception e) {
                        logger.warn("Exception while closing a QueryExecution that has reached its retrieval timeout", e);
                    }
                }
            };

            retrievalTimer.schedule(retrievalTask, delay);
        }
    }

    public synchronized void stopRetrieval() {
        if (retrievalTask != null) {
            retrievalTask.cancel();
        }

    }

    /**
     * Set a timeout on the query execution.
     * Processing will be aborted after the timeout (which starts when the approprate exec call is made).
     * Not all query execution systems support timeouts.
     * A timeout of less than zero means no timeout.
     */
    public void setTimeout(long timeout, TimeUnit timeoutUnits) {
        this.maxExecutionTime = new Time(timeout, timeoutUnits);
    }

    /**
     * Set time, in milliseconds
     *
     * @see #setTimeout(long, java.util.concurrent.TimeUnit)
     */
    public void setTimeout(long timeout) {
        this.setTimeout(timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * Set timeouts on the query execution; the first timeout refers to time to first result,
     * the second refers to overall query execution after the first result.
     * Processing will be aborted if a timeout expires.
     * Not all query execution systems support timeouts.
     * A timeout of less than zero means no timeout; this can be used for timeout1 or timeout2.
     */
    public void setTimeout(long timeout1, TimeUnit timeUnit1, long timeout2, TimeUnit timeUnit2) {
        setTimeout(timeout1, timeUnit1);

        this.maxRetrievalTime = new Time(timeout2, timeUnit2);
    }

    /**
     * Set time, in milliseconds
     *
     * @see #setTimeout(long, java.util.concurrent.TimeUnit, long, java.util.concurrent.TimeUnit)
     */
    public void setTimeout(long timeout1, long timeout2) {
        setTimeout(timeout1, TimeUnit.MILLISECONDS, timeout2, TimeUnit.MILLISECONDS);
    }
}
