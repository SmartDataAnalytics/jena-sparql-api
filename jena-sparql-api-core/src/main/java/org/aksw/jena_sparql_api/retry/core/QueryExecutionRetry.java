package org.aksw.jena_sparql_api.retry.core;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.aksw.jena_sparql_api.core.QueryExecutionDecorator;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;

import com.nurkiewicz.asyncretry.AsyncRetryExecutor;
import com.nurkiewicz.asyncretry.RetryExecutor;
import com.nurkiewicz.asyncretry.backoff.Backoff;
import com.nurkiewicz.asyncretry.policy.RetryPolicy;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

public class QueryExecutionRetry
	extends QueryExecutionDecorator
{	
	private int retryCount;
	private long retryDelayInMs;
	private RetryPolicy retryPolicy;
	private Backoff backoff;
	private boolean fixedDelay;
	
	
	public QueryExecutionRetry(QueryExecution decoratee, int retryCount, long retryDelayInMs) {
		super(decoratee);
		this.retryCount = retryCount;
		this.retryDelayInMs = retryDelayInMs;
	}
	
	public QueryExecutionRetry(QueryExecution decoratee, RetryPolicy retryPolicy, Backoff backoff, boolean fixedDelay) {
		super(decoratee);
		
		this.retryPolicy = retryPolicy;
		this.backoff = backoff;
		this.fixedDelay = fixedDelay;
	}

	public <T> T doTry(Callable<T> callable) {
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		RetryExecutor executor = new AsyncRetryExecutor(scheduler, retryPolicy, backoff, fixedDelay);
		
		ListenableFuture<T> future = executor.getWithRetry(callable);
//		scheduler.shutdown();
		try {
			T result = future.get();
			return result;
		} catch (Exception e) {
			throw new RuntimeException("Query Execution failed, even with retries.", e);
		} finally {
			scheduler.shutdown();
		}
		
//		CallableRetry<T> retry = new CallableRetry<T>(callable, retryCount, retryDelayInMs);
//		try {
//			T result = retry.call();
//			return result;
//		} catch(Exception e) {
//			throw new RuntimeException(e);
//		}
	}
	
	@Override
	public boolean execAsk() {
		return doTry(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return QueryExecutionRetry.super.execAsk(); 
			}
		});
	}

	@Override
	public ResultSet execSelect() {
		return doTry(new Callable<ResultSet>() {
			@Override
			public ResultSet call() throws Exception {
				return QueryExecutionRetry.super.execSelect(); 
			}
		});
	}

	@Override
	public Model execConstruct() {
		return doTry(new Callable<Model>() {
			@Override
			public Model call() throws Exception {
				return QueryExecutionRetry.super.execConstruct(); 
			}
		});
	}

	@Override
	public Model execConstruct(final Model model) {
		return doTry(new Callable<Model>() {
			@Override
			public Model call() throws Exception {
				return QueryExecutionRetry.super.execConstruct(model); 
			}
		});
	}

	@Override
	public Iterator<Triple> execConstructTriples() {
		return doTry(new Callable<Iterator<Triple>>() {
			@Override
			public Iterator<Triple> call() throws Exception {
				return QueryExecutionRetry.super.execConstructTriples(); 
			}
		});
	}

	@Override
	public Model execDescribe() {
		return doTry(new Callable<Model>() {
			@Override
			public Model call() throws Exception {
				return QueryExecutionRetry.super.execDescribe(); 
			}
		});
	}

	@Override
	public Model execDescribe(final Model model) {
		return doTry(new Callable<Model>() {
			@Override
			public Model call() throws Exception {
				return QueryExecutionRetry.super.execDescribe(model); 
			}
		});
	}

	@Override
	public Iterator<Triple> execDescribeTriples() {
		return doTry(new Callable<Iterator<Triple>>() {
			@Override
			public Iterator<Triple> call() throws Exception {
				return QueryExecutionRetry.super.execDescribeTriples(); 
			}
		});
	}
}