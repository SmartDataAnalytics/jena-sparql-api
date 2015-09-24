package org.aksw.jena_sparql_api.beans.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class JsonTransformerUtils {

	public static Object toJavaObject(JsonPrimitive p) {
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

	public static Object toJavaObject(JsonElement json) {
	    	Object result;
	
	//    	if(json == null) {
	//    		result = null;
	    	if(json.isJsonNull()) {
	    		result = null;
	    	} else if(json.isJsonPrimitive()) {
	    		JsonPrimitive p = json.getAsJsonPrimitive();
	    		result = toJavaObject(p);
	    	} else if(json.isJsonArray()) {
	    		JsonArray arr = json.getAsJsonArray();
	    		List<Object> tmp = new ArrayList<Object>(arr.size());
	
	    		for(JsonElement item : arr) {
	    			Object i = toJavaObject(item);
	    			tmp.add(i);
	    		}
	    		result = tmp;
	    	} else if(json.isJsonObject()) {
	    		JsonObject obj = json.getAsJsonObject();
	    		Map<String, Object> tmp = new HashMap<String, Object>();
	    		for(Entry<String, JsonElement> entry : obj.entrySet()) {
	    			String key = entry.getKey();
	    			JsonElement val = entry.getValue();
	
	    			Object o = toJavaObject(val);
	    			tmp.put(key, o);
	    		}
	    		result = tmp;
	    	} else {
	    		throw new RuntimeException("Unknown json object: " + json);
	    	}
	
	    	return result;
	    }

}
