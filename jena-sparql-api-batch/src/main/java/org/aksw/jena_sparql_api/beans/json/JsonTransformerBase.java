package org.aksw.jena_sparql_api.beans.json;

import com.google.gson.JsonElement;

/**
 * A Json transformer is a function that maps a JsonElement to some other object of type T.
 *
 * Standard use case for transformers are either rewriting json elements or converting them to objects comprising standard java classes (maps, lists, numerics, strings, null)
 *
 *
 * @author raven
 *
 * @param <T>
 */
public abstract class JsonTransformerBase<T>
	implements JsonTransformer<T>, JsonVisitor<T>
{
	@Override
	public T apply(JsonElement json) {
		T result = JsonWalker.visit(json, this);
		return result;

	}
}
