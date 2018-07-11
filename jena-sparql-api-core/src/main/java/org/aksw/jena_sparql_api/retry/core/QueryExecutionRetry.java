package org.aksw.jena_sparql_api.retry.core;

import com.google.common.util.concurrent.ListenableFuture;
import com.nurkiewicz.asyncretry.AsyncRetryExecutor;
import com.nurkiewicz.asyncretry.RetryExecutor;
import com.nurkiewicz.asyncretry.backoff.Backoff;
import com.nurkiewicz.asyncretry.policy.AbortRetryException;
import com.nurkiewicz.asyncretry.policy.RetryPolicy;
import org.aksw.jena_sparql_api.core.QueryExecutionDecorator;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.Context;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class QueryExecutionRetry
	extends QueryExecutionDecorator
{
	protected Supplier<QueryExecution> supplier;
	protected int retryCount;
	protected long retryDelayInMs;
	protected RetryPolicy retryPolicy;
	protected Backoff backoff;
	protected boolean fixedDelay;
	protected Supplier<ScheduledExecutorService> scheduler;

	protected boolean aborted = false;
	private QueryExecutionRetryDecorateeProxy proxy;

	public QueryExecutionRetry(Supplier<QueryExecution> supplier, RetryPolicy retryPolicy, Backoff backoff, boolean fixedDelay, Supplier<ScheduledExecutorService> scheduler) {
		super(new QueryExecutionRetryDecorateeProxy());

		this.supplier = supplier;
		this.retryPolicy = retryPolicy;
		this.backoff = backoff;
		this.fixedDelay = fixedDelay;
		this.scheduler = scheduler;
		((QueryExecutionRetryDecorateeProxy) this.decoratee).setOwner(this);
	}

	public <T> T resolve(Callable<T> callable) {
		if(decoratee instanceof QueryExecutionRetryDecorateeProxy) {
			this.proxy = (QueryExecutionRetryDecorateeProxy) decoratee;
			decoratee = supplier.get();
			if (proxy.getInitialBinding() != null)
				decoratee.setInitialBinding(proxy.getInitialBinding());
			if (proxy.getTimeout1() > -1L || proxy.getTimeout2() > -1L)
				decoratee.setTimeout(proxy.getTimeout1(), proxy.getTimeout2());
		}
		try {
			return callable.call();
		} catch(Exception e) {
			if(aborted) {
				throw new AbortRetryException();
			}

			decoratee = this.proxy;
			this.proxy = null;
			throw new RuntimeException(e);
		}
	}

	public <T> T doTry(Callable<T> callable) {
		Callable<T> wrapper = () -> resolve(callable);

		ScheduledExecutorService service = scheduler.get();
		RetryExecutor executor = new AsyncRetryExecutor(service, retryPolicy, backoff, fixedDelay);

		ListenableFuture<T> future = executor.getWithRetry(wrapper);
//		scheduler.shutdown();
		try {
			T result = future.get();
			return result;
		} catch (Exception e) {
			throw new RuntimeException("Query Execution failed, even with retries.", e);
		} finally {
			service.shutdown();
		}

//		CallableRetry<T> retry = new CallableRetry<T>(callable, retryCount, retryDelayInMs);
//		try {
//			T result = retry.call();
//			return result;
//		} catch(Exception e) {
//			throw new RuntimeException(e);
//		}
	}

	// TODO Maybe add synchronization so that we ensure that we do not obtain a new qe from the supplier
	// immediately after having called abort
	@Override
	public void abort() {
		aborted = true;
		if(decoratee != null) {
			decoratee.abort();
		}
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

	private static class QueryExecutionRetryDecorateeProxy implements QueryExecution {
		private QueryExecutionRetry owner;
		private QuerySolution initialBinding;
		private long timeout1 = -1L;
		private long timeout2 = -1L;

		@Override
		public void setInitialBinding(QuerySolution querySolution) {
			this.initialBinding = querySolution;
		}

		public QuerySolution getInitialBinding() {
			return initialBinding;
		}

		@Override
		public Dataset getDataset() {
			return owner.resolve(owner::getDataset);
		}

		@Override
		public Context getContext() {
			return owner.resolve(owner::getContext);
		}

		@Override
		public Query getQuery() {
			return owner.resolve(owner::getQuery);
		}

		@Override
		public ResultSet execSelect() {
			return owner.execSelect();
		}

		@Override
		public Model execConstruct() {
			return owner.execConstruct();
		}

		@Override
		public Model execConstruct(Model model) {
			return owner.execConstruct(model);
		}

		@Override
		public Iterator<Triple> execConstructTriples() {
			return owner.execConstructTriples();
		}

		@Override
		public Iterator<Quad> execConstructQuads() {
			return owner.resolve(owner::execConstructQuads);
		}

		@Override
		public Dataset execConstructDataset() {
			return owner.resolve(owner::execConstructDataset);
		}

		@Override
		public Dataset execConstructDataset(Dataset dataset) {
			return owner.resolve(() -> owner.execConstructDataset(dataset));
		}

		@Override
		public Model execDescribe() {
			return owner.execDescribe();
		}

		@Override
		public Model execDescribe(Model model) {
			return owner.execDescribe(model);
		}

		@Override
		public Iterator<Triple> execDescribeTriples() {
			return owner.execDescribeTriples();
		}
		
		@Override
		public boolean execAsk() {
			return owner.execAsk();
		}
		
		@Override
		public JsonArray execJson() {
			return owner.execJson();
		}

		@Override
		public Iterator<JsonObject> execJsonItems() {
			return owner.execJsonItems();
		}

		@Override
		public void abort() {
			owner.abort();
		}

		@Override
		public void close() {
			owner.close();
		}

		@Override
		public boolean isClosed() {
			return owner.isClosed();
		}

		public void setTimeout(long timeout, TimeUnit timeUnit) {
			long x = asMillis(timeout, timeUnit);
			this.timeout1 = -1L;
			this.timeout2 = x;
		}

		public void setTimeout(long timeout) {
			this.setTimeout(timeout, TimeUnit.MILLISECONDS);
		}

		public void setTimeout(long timeout1, TimeUnit timeUnit1, long timeout2, TimeUnit timeUnit2) {
			long x1 = asMillis(timeout1, timeUnit1);
			long x2 = asMillis(timeout2, timeUnit2);
			this.timeout1 = x1;
			if (timeout2 < 0L) {
				this.timeout2 = -1L;
			} else {
				this.timeout2 = x2;
			}

		}

		public void setTimeout(long timeout1, long timeout2) {
			this.setTimeout(timeout1, TimeUnit.MILLISECONDS, timeout2, TimeUnit.MILLISECONDS);
		}

		private static long asMillis(long duration, TimeUnit timeUnit) {
			return duration < 0L ? duration : timeUnit.toMillis(duration);
		}

		@Override
		public long getTimeout1() {
			return this.timeout1;
		}

		@Override
		public long getTimeout2() {
			return this.timeout2;
		}

		public void setOwner(QueryExecutionRetry owner) {
			this.owner = owner;
		}
	}
}
