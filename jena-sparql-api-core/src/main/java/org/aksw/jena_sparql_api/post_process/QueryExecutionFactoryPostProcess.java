package org.aksw.jena_sparql_api.post_process;

import java.util.function.Consumer;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDecorator;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

public class QueryExecutionFactoryPostProcess
    extends QueryExecutionFactoryDecorator
{
    protected Consumer<QueryExecution> postProcessor;

    public QueryExecutionFactoryPostProcess(QueryExecutionFactory decoratee, Consumer<QueryExecution> postProcessor) {
        super(decoratee);
        this.postProcessor = postProcessor;
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        QueryExecution result = super.createQueryExecution(query);
        postProcessor.accept(result);
        return result;
    }

    @Override
    public QueryExecution createQueryExecution(String queryStr) {
        QueryExecution result = super.createQueryExecution(queryStr);
        postProcessor.accept(result);
        return result;
    }
}
