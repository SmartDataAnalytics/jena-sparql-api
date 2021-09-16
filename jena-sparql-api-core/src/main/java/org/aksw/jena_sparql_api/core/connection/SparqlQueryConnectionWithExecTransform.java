package org.aksw.jena_sparql_api.core.connection;

import java.util.function.Function;

import org.aksw.jena_sparql_api.core.connection.SparqlQueryConnectionTmp;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

public class SparqlQueryConnectionWithExecTransform
	implements SparqlQueryConnectionTmp
{
	protected SparqlQueryConnection delegate;
	protected Function<? super QueryExecution, ? extends QueryExecution> execTransform;
	
	public SparqlQueryConnectionWithExecTransform(SparqlQueryConnection delegate,
			Function<? super QueryExecution, ? extends QueryExecution> execTransform) {
		super();
		this.delegate = delegate;
		this.execTransform = execTransform;
	}
	
	@Override
	public SparqlQueryConnection getDelegate() {
		return delegate;
	}

	@Override
	public QueryExecution query(Query query) {
		QueryExecution before = delegate.query(query);
		QueryExecution after = execTransform.apply(before);
		return after;
	}

	@Override
	public void close() {
		delegate.close();
	}
}
