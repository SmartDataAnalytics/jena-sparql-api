package org.aksw.jena_sparql_api.core;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 12:53 PM
 */
public class QueryExecutionFactoryDecorator
    implements QueryExecutionFactory
{
    protected QueryExecutionFactory decoratee;

    public QueryExecutionFactoryDecorator(QueryExecutionFactory decoratee) {
        this.decoratee = decoratee;
    }

    @Override
    public String getId() {
        return decoratee.getId();
    }

    @Override
    public String getState() {
        return decoratee.getState();
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        return decoratee.createQueryExecution(query);
    }

    @Override
    public QueryExecution createQueryExecution(String queryString) {
        return decoratee.createQueryExecution(queryString);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unwrap(Class<T> clazz) {
        T result;
        if(getClass().isAssignableFrom(clazz)) {
            result = (T)this;
        }
        else {
            result = decoratee.unwrap(clazz);
        }

        return result;
    }

    @Override
    public void close() throws Exception {
        decoratee.close();
    }
}

