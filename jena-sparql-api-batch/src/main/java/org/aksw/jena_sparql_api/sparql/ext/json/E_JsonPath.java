package org.aksw.jena_sparql_api.sparql.ext.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase2;
import com.jayway.jsonpath.JsonPath;

/**
 * jsonLiteral jsonp(jsonLiteral, queryString)
 *
 * @author raven
 *
 */
public class E_JsonPath
    extends FunctionBase2
{
    private Gson gson;

    public E_JsonPath() {
        this(new Gson());
    }

    public E_JsonPath(Gson gson) {
        super();
        this.gson = gson;
    }

    public static Object asJson(NodeValue nv, Gson gson) {
    	Node asNode = nv.asNode();
        Object result;
        if(nv instanceof NodeValueJson) {
            result = ((NodeValueJson)nv).getJson();
        } else if(asNode.getLiteralDatatype() instanceof RDFDatatypeJson) {
            result = asNode.getLiteralValue();
        } else if(nv.isString()) {
            String str = nv.getString();
            result = gson.fromJson(str, Object.class);
        } else {
            result = null;
        }

        return result;
    }

    public static Object jsonToObject(JsonElement json) {
    	Object result;

//    	if(json == null) {
//    		result = null;
    	if(json.isJsonNull()) {
    		result = null;
    	} else if(json.isJsonPrimitive()) {
    		JsonPrimitive p = json.getAsJsonPrimitive();
    		result = primitiveJsonToObject(p);
    	} else if(json.isJsonArray()) {
    		JsonArray arr = json.getAsJsonArray();
    		List<Object> tmp = new ArrayList<Object>(arr.size());

    		for(JsonElement item : arr) {
    			Object i = jsonToObject(item);
    			tmp.add(i);
    		}
    		result = tmp;
    	} else if(json.isJsonObject()) {
    		JsonObject obj = json.getAsJsonObject();
    		Map<String, Object> tmp = new HashMap<String, Object>();
    		for(Entry<String, JsonElement> entry : obj.entrySet()) {
    			String key = entry.getKey();
    			JsonElement val = entry.getValue();

    			Object o = jsonToObject(val);
    			tmp.put(key, o);
    		}
    		result = tmp;
    	} else {
    		throw new RuntimeException("Unknown json object: " + json);
    	}

    	return result;
    }

    public static Object primitiveJsonToObject(JsonPrimitive p) {
    	Object result;
    	if(p.isNumber()) {
    		result = p.getAsNumber();
    	} else if(p.isString()) {
    		result = p.getAsString();
    	} else if(p.isBoolean()) {
    		result = p.getAsBoolean();
    		return result;
    	} else {
    		throw new RuntimeException("Unknown type " + p);
    	}

    	return result;
    }

    public static NodeValue jsonToNodeValue(JsonElement e) {
    	NodeValue result;
    	if(e == null) {
    		result = NodeValue.nvNothing;
    	} else if(e.isJsonPrimitive()) {
    		JsonPrimitive p = e.getAsJsonPrimitive();
    		Object o = primitiveJsonToObject(p);

    		if(o != null) {
	        	RDFDatatype dtype = TypeMapper.getInstance().getTypeByValue(o);
	        	Node node = NodeFactory.createUncachedLiteral(o, dtype);
	        	result = NodeValue.makeNode(node);
//    		} else if(p.isString()) {
//    			String str = p.getAsString();
//    			result = NodeValue.makeString(str);
    		} else {
    			throw new RuntimeException("Datatype not supported " + p);
    		}
        } else if(e.isJsonObject() || e.isJsonArray()) { // arrays are json objects / array e.isJsonArray() ||
        	result = new NodeValueJson(e);
        } else {
        	throw new RuntimeException("Datatype not supported " + e);
        }

        return result;
    }

    public static NodeValue jsonToNodeValue(Object o) {
    	NodeValue result;
    	if(o == null) {
    		result = NodeValue.nvNothing;
    	} else if(o instanceof Number) {
        	RDFDatatype dtype = TypeMapper.getInstance().getTypeByValue(o);
        	Node node = NodeFactory.createUncachedLiteral(o, dtype);
        	result = NodeValue.makeNode(node);
        } else if(o instanceof String) {
        	result = NodeValue.makeString((String)o);
        } else {
            result = new NodeValueJson(o);
        }

        return result;
    }

    @Override
    public NodeValue exec(NodeValue nv, NodeValue query) {

    	Object json = asJson(nv, gson);

        NodeValue result;
        if(query.isString() && json != null) {
            String queryStr = query.getString();

            Object o = JsonPath.read(json, queryStr);
            result = jsonToNodeValue(o);
        } else {
            result = NodeValue.nvNothing;
        }

        return result;
    }
}