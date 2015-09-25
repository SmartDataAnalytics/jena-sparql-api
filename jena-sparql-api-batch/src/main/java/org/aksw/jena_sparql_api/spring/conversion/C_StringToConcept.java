package org.aksw.jena_sparql_api.spring.conversion;

import org.aksw.jena_sparql_api.batch.cli.main.AutoRegistered;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.springframework.core.convert.converter.Converter;


@AutoRegistered
public class C_StringToConcept
	implements Converter<String, Concept>
{
    public Concept convert(String str) {
    	Concept result = Concept.parse(str);
    	return result;
    }
}