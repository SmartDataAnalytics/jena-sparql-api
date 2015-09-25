package org.aksw.jena_sparql_api.beans.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonVisitorRewriteJson
	extends JsonVisitorRewrite
{
	@Override
	public JsonElement visit(JsonObject json) {
		JsonElement result;
		if(json.has("$json")) {
			JsonElement $json = json.get("$json");

			Gson gson = new Gson();
			String str = gson.toJson($json);

			JsonObject o = new JsonObject();
			o.addProperty("type", "json");
			o.addProperty("ctor", str);

			result = o;
		} else {
			result = json;
		}

		return result;
	}
}
