package org.aksw.jena_sparql_api.core.connection;

import java.util.function.Consumer;

import org.apache.jena.rdfconnection.SparqlUpdateConnection;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

/**
 * TODO Non-functional class; ISSUE Unlike SparqlQueryConnection, the SparqlUpdateConnection API does not provide access to the UpdateProcessor
 * 
 * @author raven
 *
 */
public class SparqlUpdateConnectionWithPostProcessor
	implements SparqlUpdateConnectionTmp
{
	protected SparqlUpdateConnection delegate;
	protected Consumer<? super UpdateProcessor> postProcessor;
	
	public SparqlUpdateConnectionWithPostProcessor(SparqlUpdateConnection delegate,
			Consumer<? super UpdateProcessor> postProcessor) {
		super();
		this.delegate = delegate;
		this.postProcessor = postProcessor;
	}
	
	@Override
	public SparqlUpdateConnection getDelegate() {
		return delegate;
	}
	
	@Override
	public void update(UpdateRequest updateRequest) {
		delegate.update(updateRequest);
//		UpdateProcessor result = delegate.update(updateRequest);
//		postProcessor.accept(result);
	}
	
	@Override
	public void close() {
		delegate.close();
	}
}