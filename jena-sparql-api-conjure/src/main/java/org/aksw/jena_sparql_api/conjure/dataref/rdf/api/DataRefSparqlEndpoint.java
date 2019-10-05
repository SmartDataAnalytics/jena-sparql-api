package org.aksw.jena_sparql_api.conjure.dataref.rdf.api;

import java.util.List;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefSparqlEndpoint;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.ModelFactory;

@ResourceView
@RdfType
public interface DataRefSparqlEndpoint
	extends PlainDataRefSparqlEndpoint, DataRef
{
	@Iri("eg:serviceUrl")
	DataRefSparqlEndpoint setServiceUrl(String serviceUrl);
	
	@Override
	@Iri("eg:namedGraph")
	List<String> getNamedGraphs();

	@Override
	@Iri("eg:namedGraph")
	List<String> getDefaultGraphs();

	@Override
	default <T> T accept2(DataRefVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	public static DataRefSparqlEndpoint create(String serviceUrl) {
		DataRefSparqlEndpoint result = ModelFactory.createDefaultModel().createResource().as(DataRefSparqlEndpoint.class)
				.setServiceUrl(serviceUrl);
		return result;

	}
}
