package org.aksw.jena_sparql_api.transform;

import java.util.function.Function;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDecorator;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

public class QueryExecutionFactoryQueryTransform
    extends QueryExecutionFactoryDecorator
{
    protected Function<? super Query, ? extends Query> transform;

    public QueryExecutionFactoryQueryTransform(QueryExecutionFactory decoratee, Function<? super Query, ? extends Query> transform) {
        super(decoratee);
        this.transform = transform;
    }

    @Override
    public QueryExecution createQueryExecution(String queryString) {
        throw new RuntimeException("Query must be parsed");
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        Query tmp = transform.apply(query);
        QueryExecution result = super.createQueryExecution(tmp);
        return result;
    }
}
