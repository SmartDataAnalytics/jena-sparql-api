package org.aksw.jena_sparql_api.beans.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public abstract class JsonTransformerBase<T>
	implements JsonTransformer<T>
{

	@Override
	public T apply(JsonElement json) {
		T result;

		if(json.isJsonNull()) {
			result = null;
		} else if(json.isJsonPrimitive()) {
			JsonPrimitive p = json.getAsJsonPrimitive();
			result = apply(p);
		} else if(json.isJsonArray()) {
			JsonArray arr = json.getAsJsonArray();
			result = apply(arr);
		} else if(json.isJsonObject()) {
			JsonObject obj = json.getAsJsonObject();
			result = apply(obj);
		} else {
			throw new RuntimeException("Unexpected json type: " + json);
		}


		return result;
	}

	public abstract T apply(JsonNull json);
	public abstract T apply(JsonObject json);
	public abstract T apply(JsonArray json);
	public abstract T apply(JsonPrimitive json);
}
