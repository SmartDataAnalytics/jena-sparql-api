package org.aksw.jena_sparql_api.spring.conversion;

import org.aksw.jena_sparql_api.stmt.SparqlQueryParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;

import com.hp.hpl.jena.query.Query;

@AutoRegistered
public class C_StringToQuery
    implements Converter<String, Query>
{
    @Autowired
    private SparqlQueryParser parser;

//    public C_StringToQuery() {
//        System.out.println("Created class " + this.getClass().getName());
//    }

    public SparqlQueryParser getParser() {
        return parser;
    }

    public void setParser(SparqlQueryParser parser) {
        this.parser = parser;
    }

    public Query convert(String str) {
        Query result = parser.apply(str);
        return result;
    }
}