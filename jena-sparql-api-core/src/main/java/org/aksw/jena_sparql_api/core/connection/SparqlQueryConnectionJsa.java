package org.aksw.jena_sparql_api.core.connection;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.core.Transactional;

public class SparqlQueryConnectionJsa
	extends TransactionalDelegate
	implements SparqlQueryConnectionTmp
{
	protected QueryExecutionFactory queryExecutionFactory;

    public SparqlQueryConnectionJsa(QueryExecutionFactory queryExecutionFactory) {
    	this(queryExecutionFactory, new TransactionalTmp() {});
    }

    public SparqlQueryConnectionJsa(QueryExecutionFactory queryExecutionFactory, Transactional transactional) {
        super(transactional);
        this.queryExecutionFactory = queryExecutionFactory;
    }

    @Override
    public QueryExecution query(Query query) {
        QueryExecution result = queryExecutionFactory.createQueryExecution(query);
        return result;
    }

    @Override
    public QueryExecution query(String queryStr) {
        QueryExecution result = queryExecutionFactory.createQueryExecution(queryStr);
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
