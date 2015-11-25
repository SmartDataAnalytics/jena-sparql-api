package org.aksw.jena_sparql_api.batch.json.domain;

import org.aksw.gson.utils.JsonUtils;
import org.aksw.gson.utils.JsonVisitorRewrite;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonVisitorRewriteClass
    extends JsonVisitorRewrite
{
    protected String jsonKey;
    protected String className;
    protected Gson gson;

    public JsonVisitorRewriteClass(String jsonKey, String className) {
        this(jsonKey, className, new Gson());
    }

    public JsonVisitorRewriteClass(String jsonKey, String className, Gson gson) {
        this.jsonKey = jsonKey;
        this.className = className;
        this.gson = gson;
    }

    @Override
    public JsonElement visit(JsonObject json) {
        JsonElement result;
        if(json.has(jsonKey)) {
            JsonObject tmp = json.get(jsonKey).getAsJsonObject();

            JsonObject o = new JsonObject();

            JsonUtils.extend(o, tmp);
            o.addProperty("type", className);

            result = o;
        } else {
            result = json;
        }

        return result;
    }
}
