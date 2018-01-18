package org.aksw.jena_sparql_api.core.connection;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

public class QueryExecutionFactorySparqlQueryConnection
	implements QueryExecutionFactory
{
	protected SparqlQueryConnection conn;
	protected boolean closeDelegateOnClose;

	public QueryExecutionFactorySparqlQueryConnection(SparqlQueryConnection conn) {
		this(conn, true);
	}

	public QueryExecutionFactorySparqlQueryConnection(SparqlQueryConnection conn, boolean closeDelegate) {
		super();
		this.conn = conn;
		this.closeDelegateOnClose = closeDelegate;
	}

	@Override
	public QueryExecution createQueryExecution(String queryString) {
		return conn.query(queryString);
	}

	@Override
	public QueryExecution createQueryExecution(Query query) {
		return conn.query(query);
	}

	@Override
	public void close() throws Exception {
		if(closeDelegateOnClose) {
			conn.close();
		}
	}

	@Override
	public String getId() {
		return null;
	}

	@Override
	public String getState() {
		return null;
	}

	@Override
	public <T> T unwrap(Class<T> clazz) {
        @SuppressWarnings("unchecked")
		T result = getClass().isAssignableFrom(clazz) ? (T)this : null;
        return result;
	}
}
