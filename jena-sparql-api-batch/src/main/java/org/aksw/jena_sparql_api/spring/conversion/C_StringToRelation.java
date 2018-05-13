package org.aksw.jena_sparql_api.spring.conversion;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.stmt.SparqlRelationParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;


@AutoRegistered
public class C_StringToRelation
    implements Converter<String, BinaryRelation>
{
    @Autowired
    protected SparqlRelationParser parser;

    public BinaryRelation convert(String str) {
        BinaryRelation result = parser.apply(str);
        return result;
    }
}
