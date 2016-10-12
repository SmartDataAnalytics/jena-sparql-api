package org.aksw.jena_sparql_api.retry.core;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.core.QueryExecutionDecorator;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

import com.google.common.util.concurrent.ListenableFuture;
import com.nurkiewicz.asyncretry.AsyncRetryExecutor;
import com.nurkiewicz.asyncretry.RetryExecutor;
import com.nurkiewicz.asyncretry.backoff.Backoff;
import com.nurkiewicz.asyncretry.policy.RetryPolicy;

public class QueryExecutionRetry
	extends QueryExecutionDecorator
{
	protected Supplier<QueryExecution> supplier;
	protected int retryCount;
	protected long retryDelayInMs;
	protected RetryPolicy retryPolicy;
	protected Backoff backoff;
	protected boolean fixedDelay;
	protected ScheduledExecutorService scheduler;

	public QueryExecutionRetry(Supplier<QueryExecution> supplier, int retryCount, long retryDelayInMs, ScheduledExecutorService scheduler) {
		super(null);
		this.supplier = supplier;
		this.retryCount = retryCount;
		this.retryDelayInMs = retryDelayInMs;
		this.scheduler = scheduler;
	}

	public QueryExecutionRetry(Supplier<QueryExecution> supplier, RetryPolicy retryPolicy, Backoff backoff, boolean fixedDelay, ScheduledExecutorService scheduler) {
		super(null);

		this.supplier = supplier;
		this.retryPolicy = retryPolicy;
		this.backoff = backoff;
		this.fixedDelay = fixedDelay;
		this.scheduler = scheduler;
	}


	public <T> T doTry(Callable<T> callable) {
		Callable<T> wrapper = () -> {
			if(decoratee == null) {
				decoratee = supplier.get();
			}
			try {
				return callable.call();
			} catch(Exception e) {
				decoratee = null;
				throw new RuntimeException(e);
			}
		};

		//ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		RetryExecutor executor = new AsyncRetryExecutor(scheduler, retryPolicy, backoff, fixedDelay);

		ListenableFuture<T> future = executor.getWithRetry(wrapper);
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
		return doTry(() -> super.execAsk());
	}

	@Override
	public ResultSet execSelect() {
		return doTry(() -> super.execSelect());
	}

	@Override
	public Model execConstruct() {
		return doTry(() -> super.execConstruct());
	}

	@Override
	public Model execConstruct(final Model model) {
		return doTry(() -> super.execConstruct(model));
	}

	@Override
	public Iterator<Triple> execConstructTriples() {
		return doTry(() -> super.execConstructTriples());
	}

	@Override
	public Model execDescribe() {
		return doTry(() -> super.execDescribe());
	}

	@Override
	public Model execDescribe(final Model model) {
		return doTry(() -> super.execDescribe(model));
	}

	@Override
	public Iterator<Triple> execDescribeTriples() {
		return doTry(() -> super.execDescribeTriples());
	}
}
