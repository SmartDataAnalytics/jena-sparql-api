package org.aksw.jena_sparql_api.core.connection;

import java.util.Objects;
import java.util.function.Function;

import org.aksw.jena_sparql_api.core.QueryExecutionDecorator;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

/**
 * A query connection wrapper which can raise intentional exceptions on query execution.
 * Useful for debugging behavior of application code under failure.
 *
 * @author raven
 *
 */
public class SparqlQueryConnectionWithExecFails
    extends TransactionalDelegate
    implements SparqlQueryConnectionTmp
{
    protected SparqlQueryConnection delegate;
    protected Function<? super Query, ? extends Throwable> queryToThrowable;

    public SparqlQueryConnectionWithExecFails(SparqlQueryConnection delegate,
            Function<? super Query, ? extends Throwable> queryToThrowable) {
        super();
        this.delegate = delegate;
        this.queryToThrowable = queryToThrowable;
    }

    @Override
    public SparqlQueryConnection getDelegate() {
        return delegate;
    }

    @Override
    public QueryExecution query(Query query) {
        QueryExecution core = getDelegate().query(query);
        return new QueryExecutionWithExecFails(core);
    }

    @Override
    public void close() {
    }


    public class QueryExecutionWithExecFails
        extends QueryExecutionDecorator
    {
        public QueryExecutionWithExecFails(QueryExecution delegate) {
            super(delegate);
            Objects.requireNonNull(delegate.getQuery(), "The delegate query execution must expose the query");
        }

        @Override
        protected void beforeExec() {
            super.beforeExec();

            Query query = getQuery();
            Throwable throwable = queryToThrowable.apply(query);

            if (throwable != null) {
                 throw new RuntimeException(throwable);
            }
        }
    }

}
