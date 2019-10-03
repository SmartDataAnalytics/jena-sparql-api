package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;

@ResourceView
@RdfType
public interface OpConstruct
	extends Op1
{
	@Iri
	String getQueryString();
	OpConstruct setQueryString(String queryString);
}
