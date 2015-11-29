package org.aksw.jena_sparql_api.batch.json.rewriters;

import java.util.Map.Entry;

import org.aksw.gson.utils.JsonVisitorRewrite;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 *
 *
 * @author raven
 *
 */
public class JsonVisitorRewriteSparqlPipe
    extends JsonVisitorRewrite
{
    @Override
    public JsonElement visit(JsonObject json) {
        JsonElement result;
        if(json.has("$sparqlPipe")) {
            JsonObject $sparqlPipe = json.get("$sparqlPipe").getAsJsonObject();

            JsonObject o = new JsonObject();
            o.addProperty("type", "org.aksw.jena_sparql_api.batch.step.FactoryBeanStepSparqlPipe");

            for(Entry<String, JsonElement> entry : $sparqlPipe.entrySet()) {
                o.add(entry.getKey(), entry.getValue());
            }

            result = o;
        } else {
            result = json;
        }

        return result;
    }
}