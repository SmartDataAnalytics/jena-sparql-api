package org.aksw.jena_sparql_api.beans.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Identity transformation of json elements.
 * Use this to subclass more complex transformations
 *
 * @author raven
 *
 */
public class JsonVisitorRewrite
	implements JsonVisitor<JsonElement>
{
	@Override
	public JsonElement visit(JsonNull json) {
		return json;
	}

	@Override
	public JsonElement visit(JsonObject json) {
		return json;
	}

	@Override
	public JsonElement visit(JsonArray json) {
		return json;
	}

	@Override
	public JsonElement visit(JsonPrimitive json) {
		return json;
	}
}
