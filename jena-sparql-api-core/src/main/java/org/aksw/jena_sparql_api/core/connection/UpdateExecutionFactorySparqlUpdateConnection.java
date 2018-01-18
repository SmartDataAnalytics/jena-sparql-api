package org.aksw.jena_sparql_api.core.connection;

import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;
import org.apache.jena.rdfconnection.SparqlUpdateConnection;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

public class UpdateExecutionFactorySparqlUpdateConnection
	implements UpdateExecutionFactory
{
	protected SparqlUpdateConnection conn;
	protected boolean closeDelegateOnClose;

	public UpdateExecutionFactorySparqlUpdateConnection(SparqlUpdateConnection conn) {
		this(conn, true);
	}

	public UpdateExecutionFactorySparqlUpdateConnection(SparqlUpdateConnection conn, boolean closeDelegate) {
		super();
		this.conn = conn;
		this.closeDelegateOnClose = closeDelegate;
	}

	@Override
	public UpdateProcessor createUpdateProcessor(UpdateRequest updateRequest) {
		return new UpdateProcessorRunnable(null, null, () -> conn.update(updateRequest));
	}
	

	@Override
	public UpdateProcessor createUpdateProcessor(String updateRequestStr) {
		return new UpdateProcessorRunnable(null, null, () -> conn.update(updateRequestStr));
	}

	@Override
	public void close() throws Exception {
		if(closeDelegateOnClose) {
			conn.close();
		}
	}

	@Override
	public <T> T unwrap(Class<T> clazz) {
        @SuppressWarnings("unchecked")
		T result = getClass().isAssignableFrom(clazz) ? (T)this : null;
        return result;
	}
}
