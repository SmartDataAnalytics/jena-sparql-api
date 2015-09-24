package org.aksw.jena_sparql_api.beans.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class JsonTransformerObject
	extends JsonTransformerBase<Object>
{
	@Override
	public Object apply(JsonNull json) {
		return null;
	}

	@Override
	public Object apply(JsonObject json) {
		Map<String, Object> result = new HashMap<String, Object>();
		for (Entry<String, JsonElement> entry : json.entrySet()) {
			String key = entry.getKey();
			JsonElement val = entry.getValue();

			Object o = apply(val);
			result.put(key, o);
		}
		return result;
	}

	@Override
	public Object apply(JsonArray json) {
		List<Object> result = new ArrayList<Object>(json.size());

		for (JsonElement item : json) {
			Object i = apply(item);
			result.add(i);
		}
		return result;
	}

	@Override
	public Object apply(JsonPrimitive json) {
		Object result = JsonTransformerUtils.toJavaObject(json);
		return result;
	}
}
