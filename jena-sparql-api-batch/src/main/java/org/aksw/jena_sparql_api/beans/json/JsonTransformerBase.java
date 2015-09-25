package org.aksw.jena_sparql_api.beans.json;

import com.google.gson.JsonElement;

public abstract class JsonTransformerBase<T>
	implements JsonTransformer<T>, JsonVisitor<T>
{

	@Override
	public T apply(JsonElement json) {
		T result = JsonWalker.visit(json, this);
		return result;
//		T result;
//
//		if(json.isJsonNull()) {
//			JsonNull nil = json.getAsJsonNull();
//			result = apply(nil);
//		} else if(json.isJsonPrimitive()) {
//			JsonPrimitive p = json.getAsJsonPrimitive();
//			result = apply(p);
//		} else if(json.isJsonArray()) {
//			JsonArray arr = json.getAsJsonArray();
//			result = apply(arr);
//		} else if(json.isJsonObject()) {
//			JsonObject obj = json.getAsJsonObject();
//			result = apply(obj);
//		} else {
//			throw new RuntimeException("Unexpected json type: " + json);
//		}
//
//
//		return result;
	}

//	public abstract T apply(JsonNull json);
//	public abstract T apply(JsonObject json);
//	public abstract T apply(JsonArray json);
//	public abstract T apply(JsonPrimitive json);
}
