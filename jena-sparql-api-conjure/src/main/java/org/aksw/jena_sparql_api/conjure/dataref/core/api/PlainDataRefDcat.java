package org.aksw.jena_sparql_api.conjure.dataref.core.api;

import org.apache.jena.rdf.model.Resource;

public interface PlainDataRefDcat
	extends PlainDataRef
{
	Resource getDcatRecord();
	
	@Override
	default <T> T accept(PlainDataRefVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}

}
