package org.aksw.jena_sparql_api.batch.json.domain;

import org.aksw.gson.utils.JsonUtils;
import org.aksw.gson.utils.JsonVisitorRewrite;
import org.aksw.jena_sparql_api.hop.Hop;
import org.aksw.jena_sparql_api.hop.HopQuery;
import org.aksw.jena_sparql_api.hop.HopRelation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * It appears that with a schema definition for the json, parsing would be very useful for making parsing more easy
 *
 *
 *
 */

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
            JsonElement data = json.get("$hop");
            result = processHop(data);
        } else {
            result = json;
        }
//            JsonArray arr = json.get("$hop").getAsJsonArray();
//
//            JsonObject o = new JsonObject();
//
//            o.addProperty("type", "org.aksw.jena_sparql_api.batch.step.FactoryBeanHop");
//            o.add("service", JsonUtils.safeGet(arr, 0));
//            o.add("dataset", JsonUtils.safeGet(arr, 1));
//            o.add("auth", JsonUtils.safeGet(arr, 2));
//
//            result = o;
//        } else {
//            result = json;
//        }


        return result;
    }

    public static JsonElement processHops(JsonElement json) {
        JsonArray src = json.isJsonArray() ? json.getAsJsonArray() : JsonUtils.singletonArray(json);

        JsonArray result = new JsonArray();
        for(JsonElement item : src) {
            JsonElement tmp = processHop(src);
            result.add(tmp);
        }

        return result;
    }

    public static JsonElement processHop(JsonElement json) {
        JsonElement result = processHopCore(json);
        return result;
    }

    /**
     * A hop must be a json object with the optional attributes
     * - queries
     * - relations
     *
     * @param jso
     * @return
     */
    public static JsonElement processHopCore(JsonElement json) {
        JsonObject obj = json.getAsJsonObject();

        JsonElement tmpQueries = obj.get("queries");
        JsonElement tmpRelations = obj.get("relations");

        JsonElement queries = processQueries(tmpQueries);
        JsonElement relations = processRelations(tmpRelations);

        JsonArray ctorArgs = new JsonArray();
        ctorArgs.add(queries);
        ctorArgs.add(relations);

        JsonObject result = new JsonObject();
        result.addProperty("type", Hop.class.getName());
        result.add("ctor", ctorArgs);
//            result.add("queries", queries);
//            result.add("relations", relations);

        return result;

    }



    /**
     * The json is interpreted as follows:
     * - string: a partitionedQueried to be executed on the parent service
     * - object: a single definition of a query to be run on a certain service
     * - array: if the array has either 1 or 2 string arguments it is treated similar to object (first argument is the the query, second the service)
     *   - otherwise: an array of query definitions
     *
     * If queries is an array, it can mean two things:
     *
     *
     * queries must be extended to an array of objects with attributes
     * - query : PartitionedQuery
     * - on    : SparqlService
     *
     * As a shortcut, the array can be omitted.
     *
     * @param json
     * @return
     */
    public static JsonElement processQueries(JsonElement json) {
        boolean isShortcut = isQueryShortcut(json);

        JsonArray src = isShortcut ? JsonUtils.singletonArray(json) : safeArray(json);

        JsonArray result = new JsonArray();
        for(JsonElement tmpItem : src) {
            JsonElement item = processQuery(tmpItem);
            result.add(item);
        }

        return result;
    }


    public static boolean isQueryShortcut(JsonElement json) {
        boolean isQueryShortcut = false;
        if(json != null && json.isJsonArray()) {
            JsonArray arr = json.getAsJsonArray();

            if(arr.size() <= 2) {
                JsonElement a = JsonUtils.safeGet(arr, 0);
                JsonElement b = JsonUtils.safeGet(arr, 1);

                if(a.isJsonPrimitive() && b.isJsonPrimitive()) {
                    isQueryShortcut = true;
                }
            }
        }
        return isQueryShortcut;
    }


    /**
     * two or three argument versions:
     * [ relation, hops ]
     * [ relation, service, hops ]
     *
     * (hops must be an array)
     *
     * @param json
     * @return
     */
    public static boolean isRelationShortcut(JsonElement json) {
        boolean isQueryShortcut = false;
        if(json != null && json.isJsonArray()) {
            JsonArray arr = json.getAsJsonArray();

            if(arr.size() <= 3) {
                JsonElement a = JsonUtils.safeGet(arr, 0);
                JsonElement b = JsonUtils.safeGet(arr, 1);

                if(a.isJsonPrimitive() && b.isJsonPrimitive()) {
                    isQueryShortcut = true;
                }
            }
        }
        return isQueryShortcut;
    }


    public static JsonElement processQuery(JsonElement json) {
        JsonObject obj;
        if(json.isJsonPrimitive()) {
            obj = new JsonObject();
            obj.add("query", json);
        } else if(json.isJsonArray()) {
            JsonArray arr = json.getAsJsonArray();

            obj = new JsonObject();
            obj.add("query", JsonUtils.safeGet(arr, 0));
            obj.add("on", JsonUtils.safeGet(arr, 1));
        } else {
            obj = json.getAsJsonObject();
        }

        JsonElement result = processQueryCore(obj);
        return result;
    }

    /**
     * A query object must provide the attributes
     * - query : PartitionedQuery
     * - on    : SparqlService (optional)
     * @param json
     * @return
     */
    public static JsonElement processQueryCore(JsonObject json) {
        JsonElement query = json.get("query");
        JsonElement on = json.get("on");

        JsonArray ctorArgs = new JsonArray();
        ctorArgs.add(query);
        ctorArgs.add(on);

        JsonObject result = new JsonObject();
        result.addProperty("type", HopQuery.class.getName());
        result.add("ctor", ctorArgs);

        return result;
    }

    public static JsonObject expandRelationShortuct(JsonArray arr) {
        JsonElement relation = JsonUtils.safeGet(arr, 0);

        JsonElement on;
        JsonElement hops;

        if(arr.size() == 2) {
            on = null;
            hops = JsonUtils.safeGet(arr, 1);
        } else { // arr.size == 3
            on = JsonUtils.safeGet(arr, 1);
            hops = JsonUtils.safeGet(arr, 2);
        }

        JsonObject result = new JsonObject();
        result.add("relation", relation);
        result.add("on", on);
        result.add("hops", hops);

        return result;
    }

//    public static JsonElement arrayIfNull(JsonElement json) {
//        JsonElement result = (json == null || json.isJsonNull() ? new JsonArray() : json);
//        return result;
//    }
//
//    public static JsonElement objectIfNull(JsonElement json) {
//        JsonElement result = (json == null || json.isJsonNull() ? new JsonObject() : json);
//        return result;
//    }
//
    public static JsonArray safeArray(JsonElement json) {
        JsonArray result;
        if(json == null || json.isJsonNull()) {
            result = new JsonArray();
        } else if(json.isJsonArray()) {
            result = json.getAsJsonArray();
        } else {
            throw new RuntimeException("Array expected, instead got: " + json);
        }

        return result;
    }

    public static JsonElement processRelations(JsonElement json) {

        boolean isShortcut = isRelationShortcut(json);

        JsonArray src = isShortcut ? JsonUtils.singletonArray(json) : safeArray(json);

        JsonArray result = new JsonArray();
        for(JsonElement tmpItem : src) {
            JsonElement item = processRelation(tmpItem);
            result.add(item);
        }

        return result;
    }

    public static JsonElement processRelation(JsonElement json) {
        JsonObject tmpObj = json.isJsonArray() ? expandRelationShortuct(json.getAsJsonArray()) : json.getAsJsonObject();

        JsonElement via = tmpObj.get("via");
        JsonElement on = tmpObj.get("on");
        JsonElement tmpHops = tmpObj.get("hops");

        JsonElement hops = processHops(tmpHops);

        JsonObject result = new JsonObject();
        result.add("via", via);
        result.add("on", on);
        result.add("hops", hops);
        return result;
    }

    public static JsonElement processRelationCore(JsonObject json) {
        JsonElement via = json.get("via");
        JsonElement on = json.get("on");
        JsonElement tmpHops = json.get("hops");

        JsonElement hops = processHop(tmpHops);

        JsonArray ctorArgs = new JsonArray();
        ctorArgs.add(on);
        ctorArgs.add(via);
        ctorArgs.add(hops);

        JsonObject result = new JsonObject();
        result.addProperty("type", HopRelation.class.getName());
        result.add("ctor", ctorArgs);

        return result;
    }

}