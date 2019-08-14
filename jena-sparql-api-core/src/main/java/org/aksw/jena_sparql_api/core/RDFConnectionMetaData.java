package org.aksw.jena_sparql_api.core;

import java.util.List;
import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;
import org.apache.jena.rdf.model.Resource;

public interface RDFConnectionMetaData
	extends Resource
{
	@Iri("eg:serviceURL")
	String getServiceURL();

	@IriNs("eg")
	@IriType
	List<String> getNamedGraphs();
	
	@IriNs("eg")
	@IriType
	List<String> getDefaultGraphs();
	
	RDFConnectionMetaData setServiceURL(String url);


	/**
	 * Yield information about which datasets this connection is associated with
	 * Multiple items are seen as alternative identifiers for the datasets having ideally *exactly*
	 * the same set of triples.
	 * I.e. multiple items are not to be confused with a union of their data.
	 * 
	 * TODO The items *should* be of type DcatDataset
	 * TODO Handle the case where the information available through the connection only 'roughly' corresponds to a dataset
	 * 
	 * @return
	 */
	@Iri("eg:dataset")
	Set<Resource> getDatasets();
	
	@Iri("eg:dataset")
	Resource getDataset();

}
