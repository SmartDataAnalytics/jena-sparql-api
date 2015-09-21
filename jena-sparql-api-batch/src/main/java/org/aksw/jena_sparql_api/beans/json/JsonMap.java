package org.aksw.jena_sparql_api.beans.json;

import java.util.AbstractMap;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps.EntryTransformer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import riotcmd.json;


class FnToList
	implements Function<JsonArray, List<Object>>
{
	@Override
	public List<Object> apply(JsonArray arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}

class FnMap
	implements EntryTransformer<String, JsonElement, Object>
{
	@Override
	public Object transformEntry(String key, JsonElement val) {
		return null;
	}

	public static Object toObject(JsonElement e) {
		Object result;
		if(e.isJsonArray()) {
			JsonArray a = e.getAsJsonArray();
			//a.
		}
		if(e.isJsonObject()) {
			result = new JsonMap(e.getAsJsonObject());
		} else if(e.isJsonPrimitive()) {
			JsonPrimitive p = e.getAsJsonPrimitive();
			if(p.isBoolean()) {
				result = p.getAsBoolean();
			} else if(p.isNumber()) {
				result = p.getAsNumber();
			} else if(p.isString()) {
				result = p.getAsString();
			} else {
				throw new RuntimeException("should not happen");
			}
		}
		return null;
	}
}




public class JsonMap
	extends AbstractMap<String, Object>
{
	private JsonObject json;

	public JsonMap(JsonObject json) {
		this.json = json;
	}

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
//		json.entr
//
//		Maps.transformEntries(fromMap, transformer)
//		Collections2.tr
		return null;
	}
}
