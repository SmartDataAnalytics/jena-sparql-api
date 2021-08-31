package org.aksw.jena_sparql_api.arq.core.query;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.util.Context;

public interface QueryExecutionDecorator
    extends QueryExecution
{
    QueryExecution getDelegate();

    /** Legacy method */
    @Deprecated
    default QueryExecution getDecoratee() {
        return getDelegate();
    }

    default Optional<QueryExecution> tryGetDelegate() {
        QueryExecution delegate = getDelegate();
        return Optional.ofNullable(delegate);
    }

    @Override
    default void setInitialBinding(QuerySolution binding) {
        getDelegate().setInitialBinding(binding);
    }

    @Override
    default void setInitialBinding(Binding binding) {
        getDelegate().setInitialBinding(binding);
    }

    @Override
    default Dataset getDataset() {
        return getDelegate().getDataset();
    }

    @Override
    default Context getContext() {
        // This looks hacky - the main idea is that there are cases where the delegate changes during runtime
        return tryGetDelegate().map(QueryExecution::getContext).orElse(null);
    }

    /**
     * The query associated with a query execution.
     * May be null (QueryExecution may have been created by other means)
     */
    @Override
    default Query getQuery() {
        return getDelegate().getQuery();
    }

    @Override
    default void abort() {
        getDelegate().abort();
    }

    @Override
    default void close() {
        tryGetDelegate().ifPresent(QueryExecution::close);
    }

    @Override
    default void setTimeout(long timeout, TimeUnit timeoutUnits) {
        getDelegate().setTimeout(timeout, timeoutUnits);
    }

    @Override
    default void setTimeout(long timeout) {
        getDelegate().setTimeout(timeout);
    }

    @Override
    default void setTimeout(long timeout1, TimeUnit timeUnit1, long timeout2, TimeUnit timeUnit2) {
        getDelegate().setTimeout(timeout1, timeUnit1, timeout2, timeUnit2);
    }

    @Override
    default void setTimeout(long timeout1, long timeout2) {
        getDelegate().setTimeout(timeout1, timeout2);
    }

    @Override
    default long getTimeout1() {
        return getDelegate().getTimeout1();
    }

    @Override
    default long getTimeout2() {
        return getDelegate().getTimeout2();
    }

    /* (non-Javadoc)
     * @see org.apache.jena.query.QueryExecution#isClosed()
     */
    @Override
    default boolean isClosed() {
        return getDelegate().isClosed();
    }

    @SuppressWarnings("unchecked")
    default <X> X unwrap(Class<X> clazz) {
        X result;
        if(getClass().isAssignableFrom(clazz)) {
            result = (X)this;
        }
        else {
            result = QueryExecutionDecorator.unwrap(clazz, getDelegate());
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public static <X> X unwrap(Class<X> clazz, QueryExecution qe) {
        Object tmp = qe instanceof QueryExecutionDecorator
                ? ((QueryExecutionDecorator)qe).unwrap(clazz)
                : null;
        X result = (X)tmp;
        return result;
    }

}
