package org.aksw.jena_sparql_api.core;

import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

public class UpdateExecutionFactoryPrefix
	implements UpdateExecutionFactory
{
	private UpdateExecutionFactory delegate;
	private PrefixMapping prefixMapping;
	
	public UpdateExecutionFactoryPrefix(UpdateExecutionFactory delegate, PrefixMapping prefixMapping) {
		super();
		this.delegate = delegate;
		this.prefixMapping = prefixMapping;
	}

	public UpdateExecutionFactory getDelegate() {
		return delegate;
	}

	public PrefixMapping getPrefixMapping() {
		return prefixMapping;
	}

	@Override
	public UpdateProcessor createUpdateProcessor(UpdateRequest updateRequest) {
		// TODO We should clone the request first
		updateRequest.getPrefixMapping().setNsPrefixes(prefixMapping);
		
		UpdateProcessor result = delegate.createUpdateProcessor(updateRequest);
		return result;
	}
	
	
	
}
