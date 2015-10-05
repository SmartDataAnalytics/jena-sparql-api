package org.aksw.jena_sparql_api.spring.conversion;

import org.aksw.jena_sparql_api.shape.ResourceShape;
import org.aksw.jena_sparql_api.stmt.ResourceShapeParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;

import com.google.gson.Gson;
import com.google.gson.JsonElement;


@AutoRegistered
public class C_JsonElementToResourceShape
    implements Converter<JsonElement, ResourceShape>
{
    @Autowired
    protected Gson gson;

    @Autowired
    protected ResourceShapeParser parser;
//    @Autowired
//    protected Prologue prologue;

    public ResourceShape convert(JsonElement json) {
        String str = gson.toJson(json);
        ResourceShape result = parser.apply(str);
        return result;
    }
}