package org.aksw.jena_sparql_api.spring.conversion;

import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;
import org.springframework.core.convert.converter.Converter;


@AutoRegistered
public class C_SparqlServiceToUpdateExecutionFactory
	implements Converter<SparqlService, UpdateExecutionFactory>
{
    public UpdateExecutionFactory convert(SparqlService sparqlService) {
    	UpdateExecutionFactory result = sparqlService.getUpdateExecutionFactory();
    	return result;
    }
}