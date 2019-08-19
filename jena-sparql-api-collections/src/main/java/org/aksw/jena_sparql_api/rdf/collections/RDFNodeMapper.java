package org.aksw.jena_sparql_api.rdf.collections;

import org.apache.jena.rdf.model.RDFNode;

public interface RDFNodeMapper<T>
	extends NodeConverter<RDFNode, T>
{

}
