package org.aksw.jena_sparql_api.spring.conversion;

import org.aksw.jena_sparql_api.batch.cli.main.AutoRegistered;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.springframework.core.convert.converter.Converter;


@AutoRegistered
public class C_SparqlServiceToQueryExecutionFactory
	implements Converter<SparqlService, QueryExecutionFactory>
{
    public QueryExecutionFactory convert(SparqlService sparqlService) {
    	QueryExecutionFactory result = sparqlService.getQueryExecutionFactory();
    	return result;
    }
}