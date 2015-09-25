package org.aksw.jena_sparql_api.beans.json;

import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class JsonWalker {

	public static <T> T visit(JsonElement json, JsonVisitor<T> visitor) {
		T result;
		if(json == null) {
			result = null;
		} else if(json.isJsonNull()) {
			JsonNull nil = json.getAsJsonNull();
			result = visitor.visit(nil);
		} else if(json.isJsonArray()) {
			JsonArray arr = json.getAsJsonArray();
			result = visitor.visit(arr);
		} else if(json.isJsonObject()) {
			JsonObject obj = json.getAsJsonObject();
			result = visitor.visit(obj);
		} else if(json.isJsonPrimitive()) {
			JsonPrimitive p = json.getAsJsonPrimitive();
			result = visitor.visit(p);
		} else {
			throw new RuntimeException("unknown type " + json);
		}
		return result;
	}

	/**
	 * Simple depth first traversal of a json element structure.
	 * Can be used for in-place changes.
	 * For cloning and/or rewriting the json structure, used JsonTransformerRewrite
	 *
	 * @author raven
	 *
	 */
	public static void walk(JsonElement json, JsonVisitor<?> visitor) {
		if(json == null) {
			// ignore
		} else if(json.isJsonNull()) {
			JsonNull nil = json.getAsJsonNull();
			visitor.visit(nil);
		} else if(json.isJsonArray()) {
			JsonArray arr = json.getAsJsonArray();
			visitor.visit(arr);

			for(JsonElement item : arr) {
				//visitor.visit(item);
				walk(item, visitor);
			}
		} else if(json.isJsonObject()) {
			JsonObject obj = json.getAsJsonObject();
			visitor.visit(obj);

			for(Entry<String, JsonElement> entry : obj.entrySet()) {
				JsonElement item = entry.getValue();
				walk(item, visitor);
			}
		} else if(json.isJsonPrimitive()) {
			JsonPrimitive p = json.getAsJsonPrimitive();
			visitor.visit(p);
		} else {
			throw new RuntimeException("unknown type " + json);
		}

	}

	public static JsonElement rewrite(JsonElement json, JsonVisitor<? extends JsonElement> rewriter) {
		JsonTransformerRewrite walker = new JsonTransformerRewrite(rewriter);
		JsonElement result = walker.apply(json);
		return result;
	}
//
//	public void walk(JsonElement json) {
//
//	}
}
