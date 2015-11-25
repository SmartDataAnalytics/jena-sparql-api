package org.aksw.jena_sparql_api.batch.cli.main;

import org.aksw.gson.utils.JsonVisitorRewrite;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class JsonVisitorRewriteJobParameters
    extends JsonVisitorRewrite
{
    @Override
    public JsonObject visit(JsonPrimitive json) {
        JsonObject result = new JsonObject();
        result.add("value", json);

        return result;
    }

}