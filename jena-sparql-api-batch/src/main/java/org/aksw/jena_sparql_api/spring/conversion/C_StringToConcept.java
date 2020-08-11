package org.aksw.jena_sparql_api.spring.conversion;

import org.aksw.jena_sparql_api.concept.parser.SparqlConceptParser;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;


@AutoRegistered
public class C_StringToConcept
    implements Converter<String, Concept>
{
    @Autowired
    protected SparqlConceptParser parser;

    public Concept convert(String str) {
        Concept result = parser.apply(str);
        return result;
    }
}
