package org.aksw.jena_sparql_api.spring.conversion;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.stmt.SparqlRelationParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;


@AutoRegistered
public class C_StringToRelation
    implements Converter<String, Relation>
{
    @Autowired
    protected SparqlRelationParser parser;

    public Relation convert(String str) {
        Relation result = parser.apply(str);
        return result;
    }
}
