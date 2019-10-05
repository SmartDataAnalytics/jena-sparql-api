package org.aksw.jena_sparql_api.conjure.dataref.core.api;

import java.util.List;

import org.apache.jena.sparql.core.DatasetDescription;

public interface PlainDataRefSparqlEndpoint
	extends PlainDataRef
{
	String getServiceUrl();
	List<String> getDefaultGraphs();
	List<String> getNamedGraphs();

	
	default DatasetDescription getDatsetDescription() {
		List<String> dgs = getDefaultGraphs();
		List<String> ngs = getNamedGraphs();
		
		DatasetDescription result = new DatasetDescription();
		result.addAllDefaultGraphURIs(dgs);
		result.addAllNamedGraphURIs(ngs);
		
		return result;
	}
	
	@Override
	default <T> T accept(PlainDataRefVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
}
