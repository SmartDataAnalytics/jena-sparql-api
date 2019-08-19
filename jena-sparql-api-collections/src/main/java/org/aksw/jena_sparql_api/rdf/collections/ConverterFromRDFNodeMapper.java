package org.aksw.jena_sparql_api.rdf.collections;

import org.apache.jena.rdf.model.RDFNode;


public class ConverterFromRDFNodeMapper<T>
	extends ConverterFromNodeConverter<RDFNode, T>
{
	public ConverterFromRDFNodeMapper(NodeConverter<RDFNode, T> rdfNodeConverter) {
		super(rdfNodeConverter);
	}
}
