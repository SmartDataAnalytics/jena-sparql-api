package org.aksw.jena_sparql_api.core;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;


/**
 * A query execution factory which maps all queries to select queries
 *
 * @author raven
 *
 */
public class QueryExecutionFactorySelect
    extends QueryExecutionFactoryDecorator
{
    public QueryExecutionFactorySelect(QueryExecutionFactory decoratee) {
        super(decoratee);
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        QueryExecution result = new QueryExecutionSelect(this, query, decoratee);
        return result;
    }

    //public static final Function<QueryExecutionFactory, QueryExecutionFactory> fn = new Function...
}
