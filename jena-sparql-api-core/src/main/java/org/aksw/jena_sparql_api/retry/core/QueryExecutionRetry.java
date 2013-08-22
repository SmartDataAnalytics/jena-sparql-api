package org.aksw.jena_sparql_api.retry.core;

import java.util.Iterator;
import java.util.concurrent.Callable;

import org.aksw.jena_sparql_api.core.QueryExecutionDecorator;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

public class QueryExecutionRetry
	extends QueryExecutionDecorator
{	
	private int retryCount;
	private long retryDelayInMs;
	
	public QueryExecutionRetry(QueryExecution decoratee, int retryCount, long retryDelayInMs) {
		super(decoratee);
		this.retryCount = retryCount;
		this.retryDelayInMs = retryDelayInMs;
	}

	public <T> T doTry(Callable<T> callable) {
		CallableRetry<T> retry = new CallableRetry<T>(callable, retryCount, retryDelayInMs);
		try {
			T result = retry.call();
			return result;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
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