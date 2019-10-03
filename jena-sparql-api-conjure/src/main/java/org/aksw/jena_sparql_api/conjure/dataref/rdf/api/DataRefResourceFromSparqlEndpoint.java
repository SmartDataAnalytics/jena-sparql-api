package org.aksw.jena_sparql_api.conjure.dataref.rdf.api;

import java.util.List;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefFromSparqlEndpoint;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefResource;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;

@ResourceView
@RdfType
public interface DataRefResourceFromSparqlEndpoint
	extends DataRefResource, DataRefFromSparqlEndpoint
{
	@Iri("eg:serviceUrl")
	DataRefResourceFromSparqlEndpoint setServiceUrl(String serviceUrl);
	
	@Override
	@Iri("eg:namedGraph")
	List<String> getNamedGraphs();

	@Override
	@Iri("eg:namedGraph")
	List<String> getDefaultGraphs();

	default <T> T accept(DataRefResourceVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
}
