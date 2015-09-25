package org.aksw.jena_sparql_api.beans.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class JsonTransformerBaseAdapter<T>
	extends JsonTransformerBase<T>
{
	@Override
	public T visit(JsonNull json) {
		return null;
	}

	@Override
	public T visit(JsonObject json) {
		return null;
	}

	@Override
	public T visit(JsonArray json) {
		return null;
	}

	@Override
	public T visit(JsonPrimitive json) {
		return null;
	}
}
