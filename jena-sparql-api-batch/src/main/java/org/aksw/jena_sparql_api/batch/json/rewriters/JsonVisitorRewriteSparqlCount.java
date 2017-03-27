package org.aksw.jena_sparql_api.batch.json.rewriters;

import java.util.Map.Entry;

import org.aksw.gson.utils.JsonUtils;
import org.aksw.gson.utils.JsonVisitorRewrite;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 *
 *
 * @author raven
 *
 */
public class JsonVisitorRewriteSparqlCount
    extends JsonVisitorRewrite
{

    @Override
    public JsonElement visit(JsonObject json) {
        JsonElement result;
        if(json.has("$sparqlCount")) {
            JsonObject tmp = json.get("$sparqlCount").getAsJsonObject();

            JsonObject o = new JsonObject();

            JsonUtils.extend(o, tmp);
            o.addProperty("type", "org.aksw.jena_sparql_api.batch.step.FactoryBeanStepSparqlCount");

            result = o;
        } else {
            result = json;
        }

        return result;
    }
}