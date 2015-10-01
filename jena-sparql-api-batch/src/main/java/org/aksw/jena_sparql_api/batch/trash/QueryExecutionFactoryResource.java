package org.aksw.jena_sparql_api.batch.trash;

import org.aksw.jena_sparql_api.core.QueryExecutionFactoryBackQuery;

import com.hp.hpl.jena.query.Query;

public class QueryExecutionFactoryResource
    extends QueryExecutionFactoryBackQuery
{
    protected String fileNameOrUrl;

    public QueryExecutionFactoryResource(String fileNameOrUrl) {
        this.fileNameOrUrl = fileNameOrUrl;
    }

    @Override
    public String getId() {
        return fileNameOrUrl;
    }

    @Override
    public String getState() {
        return "";
    }

    @Override
    public QueryExecutionResource createQueryExecution(Query query) {
        QueryExecutionResource result = new QueryExecutionResource(query, null, fileNameOrUrl);
        return result;
    }
}
