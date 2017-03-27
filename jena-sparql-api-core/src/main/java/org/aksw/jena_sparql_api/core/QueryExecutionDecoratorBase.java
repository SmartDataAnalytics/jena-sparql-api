package org.aksw.jena_sparql_api.core;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.Context;


/**
 * Adds beforeExec and afterExec methods that can be used
 * to allocate and release resources upon performing an execution.
 *
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 10:28 AM
 */
public class QueryExecutionDecoratorBase<T extends QueryExecution>
    implements QueryExecution
{
    protected T decoratee;

    public QueryExecutionDecoratorBase(T decoratee) {
        //this.decoratee = new QueryExecutionWrapper(decoratee);
        //this.setDecoratee(decoratee);
        this.decoratee = decoratee;
    }

    /*
    public QueryExecutionDecoratorBase(QueryExecution decoratee)
    {
        this.setDecoratee(decoratee);
        //this.decoratee = decoratee;
    }*/

    public QueryExecution getDecoratee()
    {
        return decoratee;
    }

//    protected void setDecoratee(T decoratee)
//    {
//        this.decoratee = decoratee;
//    }

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
     */
    @Override
    public Query getQuery() {
        return decoratee.getQuery();
    }

    @Override
    public void abort() {
        decoratee.abort();
    }

    @Override
    public void close() {
        if(decoratee != null) {
            decoratee.close();
        }
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
    public long getTimeout1() {
        return decoratee.getTimeout1();
    }

    @Override
    public long getTimeout2() {
        return decoratee.getTimeout2();
    }

    /* (non-Javadoc)
     * @see org.apache.jena.query.QueryExecution#isClosed()
     */
    @Override
    public boolean isClosed() {
        return decoratee.isClosed();
    }

    protected void beforeExec() {

    }

    protected void afterExec() {

    }

    protected void onException(Exception e) {
    }

    @Override
    public ResultSet execSelect() {
        beforeExec();
        try {
            return decoratee.execSelect();
        } catch(Exception e) {
        	onException(e);
        	throw new RuntimeException(e);
        } finally {
            afterExec();
        }
    }

    @Override
    public Model execConstruct() {
        beforeExec();
        try {
            return decoratee.execConstruct();
        } catch(Exception e) {
        	onException(e);
        	throw new RuntimeException(e);
        } finally {
            afterExec();
        }
    }

    @Override
    public Model execConstruct(Model model) {
        beforeExec();
        try {
            return decoratee.execConstruct(model);
        } catch(Exception e) {
        	onException(e);
        	throw new RuntimeException(e);
        } finally {
            afterExec();
        }
    }

    @Override
    public Model execDescribe() {
        beforeExec();
        try {
            return decoratee.execDescribe();
        } catch(Exception e) {
        	onException(e);
        	throw new RuntimeException(e);
        } finally {
            afterExec();
        }
    }

    @Override
    public Model execDescribe(Model model) {
        beforeExec();
        try {
            return decoratee.execDescribe(model);
        } catch(Exception e) {
        	onException(e);
        	throw new RuntimeException(e);
        } finally {
            afterExec();
        }
    }

    @Override
    public boolean execAsk() {
        beforeExec();
        try {
            return decoratee.execAsk();
        } catch(Exception e) {
        	onException(e);
        	throw new RuntimeException(e);
        } finally {
            afterExec();
        }
    }

    @Override
    public Iterator<Triple> execConstructTriples() {
        beforeExec();
        try {
            return decoratee.execConstructTriples();
        } catch(Exception e) {
        	onException(e);
        	throw new RuntimeException(e);
        } finally {
            afterExec();
        }
    }

    @Override
    public Iterator<Triple> execDescribeTriples() {
        beforeExec();
        try {
            return decoratee.execDescribeTriples();
        } catch(Exception e) {
        	onException(e);
        	throw new RuntimeException(e);
        } finally {
            afterExec();
        }
    }

    @Override
    public Iterator<Quad> execConstructQuads() {
        beforeExec();
        try {
            return decoratee.execConstructQuads();
        } catch(Exception e) {
        	onException(e);
        	throw new RuntimeException(e);
        } finally {
            afterExec();
        }
    }

    @Override
    public Dataset execConstructDataset() {
        beforeExec();
        try {
            return decoratee.execConstructDataset();
        } catch(Exception e) {
        	onException(e);
        	throw new RuntimeException(e);
        } finally {
            afterExec();
        }
    }

    @Override
    public Dataset execConstructDataset(Dataset dataset) {
        beforeExec();
        try {
            return decoratee.execConstructDataset(dataset);
        } catch(Exception e) {
        	onException(e);
        	throw new RuntimeException(e);
        } finally {
            afterExec();
        }
    }

    @SuppressWarnings("unchecked")
    public <X> X unwrap(Class<X> clazz) {
        X result;
        if(getClass().isAssignableFrom(clazz)) {
            result = (X)this;
        }
        else {
        	result = QueryExecutionDecoratorBase.unwrap(clazz, decoratee);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public static <X> X unwrap(Class<X> clazz, QueryExecution qe) {
    	Object tmp = qe instanceof QueryExecutionDecoratorBase
    			? ((QueryExecutionDecoratorBase<?>)qe).unwrap(clazz)
    			: null;
    	X result = (X)tmp;
    	return result;
    }
}
