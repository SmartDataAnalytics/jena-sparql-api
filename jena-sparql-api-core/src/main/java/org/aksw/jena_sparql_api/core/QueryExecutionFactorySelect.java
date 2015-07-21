package org.aksw.jena_sparql_api.core;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;


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
