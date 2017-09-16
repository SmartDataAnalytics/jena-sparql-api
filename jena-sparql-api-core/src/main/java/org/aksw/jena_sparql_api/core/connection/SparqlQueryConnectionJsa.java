package org.aksw.jena_sparql_api.core.connection;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

public class SparqlQueryConnectionJsa
    implements SparqlQueryConnectionTmp
{
    protected QueryExecutionFactory queryExecutionFactory;

    public SparqlQueryConnectionJsa(QueryExecutionFactory queryExecutionFactory) {
        super();
        this.queryExecutionFactory = queryExecutionFactory;
    }

    @Override
    public QueryExecution query(Query query) {
        QueryExecution result = queryExecutionFactory.createQueryExecution(query);
        return result;
    }

    @Override
    public void close() {
        try {
            queryExecutionFactory.close();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
