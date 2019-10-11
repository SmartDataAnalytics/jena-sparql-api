package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Resource;

@ResourceView
@RdfType
public interface JobBinding
	extends Resource
{
	
}
