package org.aksw.jena_sparql_api.batch.json.domain;

import org.aksw.gson.utils.JsonVisitorRewrite;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 *
 *
 * @author raven
 *
 */
public class JsonVisitorRewriteSparqlFile
    extends JsonVisitorRewrite
{
    @Override
    public JsonElement visit(JsonObject json) {
        JsonElement result;
        if(json.has("$sparqlFile")) {
            JsonElement e = json.get("$sparqlFile");

            JsonObject o = new JsonObject();
            if(e.isJsonPrimitive()) {
                o.addProperty("type", "org.aksw.jena_sparql_api.batch.step.FactoryBeanSparqlFile");
                o.add("fileNameOrUrl", e);
            } else {
                throw new RuntimeException("Argument not supported: " + json);
            }

            result = o;
        } else {
            result = json;
        }

        return result;
    }
}