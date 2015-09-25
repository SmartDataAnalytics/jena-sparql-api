package org.aksw.jena_sparql_api.beans.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public interface JsonVisitor<T> {
	T visit(JsonNull json);
	T visit(JsonObject json);
	T visit(JsonArray json);
	T visit(JsonPrimitive json);
}
