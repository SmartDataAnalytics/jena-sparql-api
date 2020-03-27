package org.aksw.jena_sparql_api.sparql.ext.json;

import java.util.Map.Entry;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprTypeException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Transform a JSON object into a array of objects with key and value attributes.
 * [{key: "someKey", value: ... }]
 *
 * @author raven
 *
 */
public class E_JsonEntries
    extends FunctionBase1
{
    protected Gson gson;

    public E_JsonEntries() {
        this(new Gson());
    }

    public E_JsonEntries(Gson gson) {
        super();
        this.gson = gson;
    }

    public static JsonArray keysToArray(JsonObject jo, String keyAttrName, String valueAttrName) {
        JsonArray result = new JsonArray();
        for(Entry<String, JsonElement> e : jo.entrySet()) {
            JsonObject item = new JsonObject();
            item.addProperty(keyAttrName, e.getKey());
            item.add(valueAttrName, e.getValue());
            result.add(item);
        }
        return result;
    }

    @Override
    public NodeValue exec(NodeValue nv) {

        NodeValue result = null;
        JsonElement json = E_JsonPath.asJson(nv);

        if(json != null) {
            if(json.isJsonObject() && !json.isJsonArray()) {
                JsonObject jo = json.getAsJsonObject();
                JsonArray arr = keysToArray(jo, "key", "value");

                RDFDatatype jsonDatatype = TypeMapper.getInstance().getTypeByClass(JsonElement.class);
                Node node = E_JsonPath.jsonToNode(arr, gson, jsonDatatype);
                result = NodeValue.makeNode(node);
            }
        }

        if(result == null) {
            NodeValue.raise(new ExprTypeException("Argument was not a json object"));
        }

        return result;
    }

}
