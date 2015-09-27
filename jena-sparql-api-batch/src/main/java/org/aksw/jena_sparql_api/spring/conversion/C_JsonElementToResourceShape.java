package org.aksw.jena_sparql_api.spring.conversion;

import org.aksw.jena_sparql_api.shape.ResourceShape;
import org.aksw.jena_sparql_api.shape.ResourceShapeParserJson;
import org.springframework.core.convert.converter.Converter;

import com.google.gson.JsonElement;


@AutoRegistered
public class C_JsonElementToResourceShape
	implements Converter<JsonElement, ResourceShape>
{
    public ResourceShape convert(JsonElement json) {
    	ResourceShape result = ResourceShapeParserJson.parse(json);
    	return result;
    }
}