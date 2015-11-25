package org.aksw.jena_sparql_api.batch.trash;

import org.aksw.jena_sparql_api.core.QueryExecutionBaseSelect;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;

public class QueryExecutionResource
    extends QueryExecutionBaseSelect
{
    protected String fileNameOrUrl;

    public QueryExecutionResource(Query query, QueryExecutionFactory subFactory, String fileNameOrUrl) {
        super(query, subFactory);
        this.fileNameOrUrl = fileNameOrUrl;
    }

    @Override
    protected QueryExecution executeCoreSelectX(Query query) {
        QueryExecution result = new QueryExecutionResourceCore(fileNameOrUrl);
        return result;
    }
}
