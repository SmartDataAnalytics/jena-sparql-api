package org.aksw.jena_sparql_api.ext.virtuoso;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.apache.jena.shared.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	public static URL createUrl(String str) {
    	URL url;
		try {
			url = new URL(str);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		return url;
	}
	
	public static void checkUrl(URL url) {
		try {
	        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			try {
		        connection.setRequestMethod("GET");
		        connection.connect();
		        int code = connection.getResponseCode();
		        if(code != 200) {
		        	//logger.info("Health check status: fail");
		        	throw new NotFoundException(url.toString());
		        }
			} finally {
				connection.disconnect();
			}
	    	//logger.info("Health check status: success");
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}