package org.aksw.jena_sparql_api.ext.virtuoso;

import java.util.concurrent.TimeUnit;

public class HealthcheckRunner
	implements Runnable
{
	protected long healthcheckRetryCount;
	protected long healthcheckInterval;
	protected TimeUnit healthcheckIntervalUnit;
	protected Runnable healthcheckAction;
	
	public HealthcheckRunner(long healthcheckRetryCount, long healthcheckInterval, TimeUnit healthcheckIntervalUnit,
			Runnable healthcheck) {
		super();
		this.healthcheckRetryCount = healthcheckRetryCount;
		this.healthcheckInterval = healthcheckInterval;
		this.healthcheckIntervalUnit = healthcheckIntervalUnit;
		this.healthcheckAction = healthcheck;
	}

	@Override
	public void run() {

		// Wait for the health check to succeed the first time
		boolean success = false;
		Exception lastException = null;

		int i = 0;
		for(; i < healthcheckRetryCount; ++i) {
			try {
				healthcheckAction.run();
				success = true;
				break;
			} catch(Exception e) {
				lastException = e;
			}
			
			try {
				healthcheckIntervalUnit.sleep(healthcheckInterval);
			} catch (InterruptedException e) {
				break;
			}
		}
		
		if(!success) {
			throw new RuntimeException("Startup considered failed after " + i + " failed health checks", lastException);
		}		
	}
}