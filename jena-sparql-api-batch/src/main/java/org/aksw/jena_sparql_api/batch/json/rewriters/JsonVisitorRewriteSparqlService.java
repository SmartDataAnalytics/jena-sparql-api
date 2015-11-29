package org.aksw.jena_sparql_api.batch.json.rewriters;

import java.util.Map.Entry;

import org.aksw.gson.utils.JsonUtils;
import org.aksw.gson.utils.JsonVisitorRewrite;
import org.aksw.jena_sparql_api.utils.ListUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 *
 *
 * @author raven
 *
 */
public class JsonVisitorRewriteSparqlService
    extends JsonVisitorRewrite
{
    @Override
    public JsonElement visit(JsonObject json) {
        JsonElement result;
        if(json.has("$sparqlService")) {
            JsonArray arr = json.get("$sparqlService").getAsJsonArray();

            JsonObject o = new JsonObject();

            o.addProperty("type", "org.aksw.jena_sparql_api.batch.step.FactoryBeanSparqlService");
            o.add("service", JsonUtils.safeGet(arr, 0));
            o.add("dataset", JsonUtils.safeGet(arr, 1));
            o.add("auth", JsonUtils.safeGet(arr, 2));

            result = o;
        } else {
            result = json;
        }

        return result;
    }
}