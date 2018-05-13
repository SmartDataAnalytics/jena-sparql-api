package org.aksw.jena_sparql_api.ext.virtuoso;

import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.Service;



public class ServiceWrapperHealthCheck
	extends AbstractIdleService
{	
	protected Service delegate;
	protected Runnable healthcheckRunner;
	
	public ServiceWrapperHealthCheck(Service delegate, Runnable healthcheckRunner) {
		super();
		this.delegate = delegate;
		this.healthcheckRunner = healthcheckRunner;
	}

	@Override
	protected void startUp() throws Exception {
		// Wait for the delegate to start
		delegate.startAsync().awaitRunning();

		healthcheckRunner.run();
	}
	
	@Override
	protected void shutDown() throws Exception {
		delegate.stopAsync();
	}
}
