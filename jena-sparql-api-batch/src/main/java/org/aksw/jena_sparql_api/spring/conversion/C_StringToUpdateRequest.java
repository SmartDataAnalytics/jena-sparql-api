package org.aksw.jena_sparql_api.spring.conversion;

import org.aksw.jena_sparql_api.stmt.SparqlUpdateParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;

import com.hp.hpl.jena.update.UpdateRequest;

@AutoRegistered
public class C_StringToUpdateRequest
    implements Converter<String, UpdateRequest>
{
    @Autowired
    private SparqlUpdateParser updateParser;

    public UpdateRequest convert(String str) {
        UpdateRequest result = updateParser.apply(str);
        return result;
    }
}