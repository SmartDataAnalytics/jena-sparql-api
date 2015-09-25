package org.aksw.jena_sparql_api.beans.json;

import com.google.common.base.Function;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class JsonRewriterBase
	implements JsonRewriter, JsonVisitor<JsonElement>
{
	private Function<JsonObject, JsonElement> jsonObjectRewriter;

	@Override
	public JsonElement apply(JsonElement input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonElement visit(JsonNull json) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonElement visit(JsonObject json) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonElement visit(JsonArray json) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonElement visit(JsonPrimitive json) {
		// TODO Auto-generated method stub
		return null;
	}

}
