package org.aksw.jena_sparql_api.batch.json.rewriters;

import org.aksw.gson.utils.JsonVisitorRewrite;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonVisitorRewritePrefixes
    extends JsonVisitorRewrite
{
    @Override
    public JsonElement visit(JsonObject json) {
        JsonElement result;
        if(json.has("$prefixes")) {
            JsonElement $shape = json.get("$prefixes");
            JsonObject $json = new JsonObject();
            $json.add("$json", $shape);

            JsonObject o = new JsonObject();
            o.addProperty("type", "org.aksw.jena_sparql_api.batch.step.FactoryBeanPrefixes");

            o.add("prefixes", $json);
            result = o;
        } else {
            result = json;
        }

        return result;
    }
}
