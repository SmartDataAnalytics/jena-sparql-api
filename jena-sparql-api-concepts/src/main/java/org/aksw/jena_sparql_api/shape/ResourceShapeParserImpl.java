package org.aksw.jena_sparql_api.shape;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.apache.jena.sparql.core.Prologue;

public class ResourceShapeParserImpl
    implements ResourceShapeParser
{
    protected Prologue prologue;
    protected Gson gson;

    public ResourceShapeParserImpl(Prologue prologue, Gson gson) {
        this.prologue = prologue;
        this.gson = gson;
    }

    @Override
    public ResourceShape apply(String str) {
        JsonElement json = gson.fromJson(str, JsonElement.class);

        ResourceShapeBuilder builder = new ResourceShapeBuilder(prologue);
        ResourceShape result = ResourceShapeParserJson.parse(json, builder);
        return result;
    }
}
