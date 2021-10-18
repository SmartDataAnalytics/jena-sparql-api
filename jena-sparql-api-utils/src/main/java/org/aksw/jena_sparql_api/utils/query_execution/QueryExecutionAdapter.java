package org.aksw.jena_sparql_api.utils.query_execution;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.util.Context;

/**
 *
 *
 * @author Claus Stadler
 *         <p/>
 *         Date: 11/29/11
 *         Time: 12:01 AM
 */
public class QueryExecutionAdapter
    implements QueryExecution
{
//    protected QueryExecutionTimeoutHelper timeoutHelper = new QueryExecutionTimeoutHelper(this);

    @Override
    public void setInitialBinding(Binding binding) {
        throw new UnsupportedOperationException("Not Implemented.");
    }

    @Override
    public void setInitialBinding(QuerySolution binding) {
        throw new UnsupportedOperationException("Not Implemented.");
    }

    @Override
    public Dataset getDataset() {
        return null;
    }

    @Override
    public Context getContext() {
        return null;
    }

    /**
     * The query associated with a query execution.
     * May be null (QueryExecution may have been created by other means)
     */
    @Override
    public Query getQuery() {
        return null;
    }

    @Override
    public String getQueryString() {
        return null;
    }

    @Override
    public ResultSet execSelect() {
        throw new UnsupportedOperationException("Not Implemented.");
    }

    @Override
    public Model execConstruct() {
        throw new UnsupportedOperationException("Not Implemented.");
    }

    @Override
    public Model execConstruct(Model model) {
        throw new UnsupportedOperationException("Not Implemented.");
    }

    @Override
    public Model execDescribe() {
        throw new UnsupportedOperationException("Not Implemented.");
    }

    @Override
    public Model execDescribe(Model model) {
        throw new UnsupportedOperationException("Not Implemented.");
    }

    @Override
    public boolean execAsk() {
        throw new UnsupportedOperationException("Not Implemented.");
    }

    @Override
    public void abort() {
    }

    @Override
    public void close() {
    }

    @Override
    public void setTimeout(long timeout, TimeUnit timeoutUnits) {
        throw new UnsupportedOperationException("Not Implemented.");
//        timeoutHelper.setTimeout(timeout, timeoutUnits);
    }

    @Override
    public void setTimeout(long timeout) {
        throw new UnsupportedOperationException("Not Implemented.");
//        timeoutHelper.setTimeout(timeout);
    }

    @Override
    public void setTimeout(long timeout1, TimeUnit timeUnit1, long timeout2, TimeUnit timeUnit2) {
        throw new UnsupportedOperationException("Not Implemented.");

//        timeoutHelper.setTimeout(timeout1, timeUnit1, timeout2, timeUnit2);
    }

    @Override
    public void setTimeout(long timeout1, long timeout2) {
        throw new UnsupportedOperationException("Not Implemented.");
//        timeoutHelper.setTimeout(timeout1, timeout2);
    }

    @Override
    public Iterator<Triple> execConstructTriples() {
        throw new UnsupportedOperationException("Not Implemented.");
    }

    @Override
    public Iterator<Triple> execDescribeTriples() {
        throw new UnsupportedOperationException("Not Implemented.");
    }

    @Override
    public long getTimeout1() {
        throw new UnsupportedOperationException("Not Implemented.");
    }

    @Override
    public long getTimeout2() {
        throw new UnsupportedOperationException("Not Implemented.");
    }

    @Override
    public boolean isClosed() {
        throw new UnsupportedOperationException("Not Implemented.");
    }

    @Override
    public Dataset execConstructDataset() {
        throw new UnsupportedOperationException("Not Implemented.");
    }

    @Override
    public Dataset execConstructDataset(Dataset arg0) {
        throw new UnsupportedOperationException("Not Implemented.");
    }

    @Override
    public Iterator<Quad> execConstructQuads() {
        throw new UnsupportedOperationException("Not Implemented.");
    }

    @Override
    public JsonArray execJson() {
        throw new UnsupportedOperationException("Not Implemented.");
    }

    @Override
    public Iterator<JsonObject> execJsonItems() {
        throw new UnsupportedOperationException("Not Implemented.");
    }
}
