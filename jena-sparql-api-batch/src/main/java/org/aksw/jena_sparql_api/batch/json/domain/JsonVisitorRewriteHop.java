package org.aksw.jena_sparql_api.batch.json.domain;

import org.aksw.gson.utils.JsonUtils;
import org.aksw.gson.utils.JsonVisitorRewrite;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 *
 *  $hop: [
 *                 [ '?l | CONSTRUCT { ?x tmp:geocodeJson ?j } WHERE { ?x tmp:geocodeJson ?j ; tmp:hasLocation ?l }', '#{ geocoderCache }'],
 *                 [ '?l | CONSTRUCT { ?x tmp:lgdLink ?l }', '#{ source }'],
 *                 { via: '?x ?l | ?x sameAs ?l',
 *                   shape: [ '?l | CONSTRUCT { ?x tmp:lgdLink ?l }', '#{ source }']
 *                 }
 *
 * type: 'org...FactoryBeanHop',
 * mappedQueries: 
 *
 *  $hop: array
 *
 *
 *  $hop: str:string
 *    -&gt;
 *
 * @author raven
 *
 */
public class JsonVisitorRewriteHop
    extends JsonVisitorRewrite
{
    @Override
    public JsonElement visit(JsonObject json) {
        JsonElement result;
        if(json.has("$hop")) {
            JsonArray arr = json.get("$hop").getAsJsonArray();

            JsonObject o = new JsonObject();

            o.addProperty("type", "org.aksw.jena_sparql_api.batch.step.FactoryBeanHop");
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