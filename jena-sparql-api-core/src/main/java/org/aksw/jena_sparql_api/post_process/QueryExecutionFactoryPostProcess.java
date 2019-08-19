package org.aksw.jena_sparql_api.post_process;

import java.util.function.Function;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDecorator;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

public class QueryExecutionFactoryPostProcess
    extends QueryExecutionFactoryDecorator
{
    protected Function<? super QueryExecution, ? extends QueryExecution> postProcessor;

    public QueryExecutionFactoryPostProcess(QueryExecutionFactory decoratee, Function<? super QueryExecution, ? extends QueryExecution> postProcessor) {
        super(decoratee);
        this.postProcessor = postProcessor;
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        QueryExecution tmp = super.createQueryExecution(query);
        QueryExecution result = postProcessor.apply(tmp);
        return result;
    }

    @Override
    public QueryExecution createQueryExecution(String queryStr) {
        QueryExecution tmp = super.createQueryExecution(queryStr);
        QueryExecution result = postProcessor.apply(tmp);
        return result;
    }
}
