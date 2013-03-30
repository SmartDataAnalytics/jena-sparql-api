package org.aksw.jena_sparql_api.core;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Triple;


public class QueryExecutionStreamingDecorator
	extends QueryExecutionDecoratorBase<QueryExecutionStreaming>
	implements QueryExecutionStreaming
{
	public QueryExecutionStreamingDecorator(QueryExecutionStreaming decoratee) {
		super(decoratee);
	}

	@Override
	public Iterator<Triple> execConstructStreaming() {
		return decoratee.execConstructStreaming();
	}
	
	@Override
	public Iterator<Triple> execDescribeStreaming() {
		return decoratee.execDescribeStreaming();
	}
}

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 10:28 AM
 */
/*
public class QueryExecutionStreamingDecorator
    implements QueryExecutionStreaming
{
    protected QueryExecutionStreaming decoratee;

    protected QueryExecutionStreaming getDecoratee()
    {
        return decoratee;
    }

    protected void setDecoratee(QueryExecutionStreaming decoratee)
    {
        this.decoratee = decoratee;
    }

    public QueryExecutionStreamingDecorator(QueryExecutionStreaming decoratee)
    {
        this.decoratee = decoratee;
    }

    @Override
    public void setFileManager(FileManager fm) {
        decoratee.setFileManager(fm);
    }

    @Override
    public void setInitialBinding(QuerySolution binding) {
        decoratee.setInitialBinding(binding);
    }

    @Override
    public Dataset getDataset() {
        return decoratee.getDataset();
    }

    @Override
    public Context getContext() {
        return decoratee.getContext();
    }

    /**
     * The query associated with a query execution.
     * May be null (QueryExecution may have been created by other means)
     * /
    @Override
    public Query getQuery() {
        return decoratee.getQuery();
    }

    @Override
    public ResultSet execSelect() {
        return decoratee.execSelect();
    }

    @Override
    public Model execConstruct() {
        return decoratee.execConstruct();
    }

    @Override
    public Model execConstruct(Model model) {
        return decoratee.execConstruct(model);
    }

    @Override
    public Model execDescribe() {
        return decoratee.execDescribe();
    }

    @Override
    public Model execDescribe(Model model) {
        return decoratee.execDescribe(model);
    }

    @Override
    public boolean execAsk() {
        return decoratee.execAsk();
    }

    @Override
    public void abort() {
        decoratee.abort();
    }

    @Override
    public void close() {
        decoratee.close();;
    }

    @Override
    public void setTimeout(long timeout, TimeUnit timeoutUnits) {
        decoratee.setTimeout(timeout, timeoutUnits);
    }

    @Override
    public void setTimeout(long timeout) {
        decoratee.setTimeout(timeout);
    }

    @Override
    public void setTimeout(long timeout1, TimeUnit timeUnit1, long timeout2, TimeUnit timeUnit2) {
        decoratee.setTimeout(timeout1, timeUnit1, timeout2, timeUnit2);
    }

    @Override
    public void setTimeout(long timeout1, long timeout2) {
        decoratee.setTimeout(timeout1, timeout2);
    }

	@Override
	public Iterator<Triple> execConstructStreaming() {
		return decoratee.execConstructStreaming();
	}

	@Override
	public Iterator<Triple> execDescribeStreaming() {
		return decoratee.execDescribeStreaming();
	}
}
*/