package org.aksw.jena_sparql_api.batch.json.domain;

import org.aksw.gson.utils.JsonVisitorRewrite;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonVisitorRewriteShape
	extends JsonVisitorRewrite
{
	@Override
	public JsonElement visit(JsonObject json) {
		JsonElement result;
		if(json.has("$shape")) {
			JsonElement $shape = json.get("$shape");
			JsonObject $json = new JsonObject();
			$json.add("$json", $shape);

			JsonObject o = new JsonObject();
			o.addProperty("type", "org.aksw.jena_sparql_api.shape.ResourceShape");

			o.add("ctor", $json);
			result = o;
		} else {
			result = json;
		}

		return result;
	}
}
