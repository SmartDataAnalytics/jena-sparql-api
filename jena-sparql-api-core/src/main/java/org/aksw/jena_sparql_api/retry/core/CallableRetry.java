package org.aksw.jena_sparql_api.retry.core;


import java.util.concurrent.Callable;

import org.aksw.jena_sparql_api.delay.extra.Delayer;
import org.aksw.jena_sparql_api.delay.extra.DelayerDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CallableRetry<T>
	implements Callable<T>
{
	private static final Logger logger = LoggerFactory.getLogger(QueryExecutionRetry.class);

	private Callable<T> callable;
	private int retryCount;
	
	private Delayer retryDelayer;
	
	public CallableRetry(Callable<T> callable, int retryCount, long retryDelayInMs) {
		this(callable, retryCount, new DelayerDefault(retryDelayInMs));
	}
	
	public CallableRetry(Callable<T> callable, int retryCount, Delayer retryDelayer) {
		this.callable = callable;
		this.retryCount = retryCount;
		this.retryDelayer = retryDelayer;
	}
	
	@Override
	public T call() throws Exception {
		int errorCount = 0;
		for(;;) {
			try {
				retryDelayer.doDelay();
				
				T result = callable.call();
				return result;
			} catch (Exception e) {
				++errorCount;
				logger.warn("Failure " + errorCount + "/" + retryCount + " [" + e.getMessage() + "]");

				if(errorCount < retryCount) {
					continue;
				}
				
				throw new RuntimeException(e);
			}
		}
	}	
}