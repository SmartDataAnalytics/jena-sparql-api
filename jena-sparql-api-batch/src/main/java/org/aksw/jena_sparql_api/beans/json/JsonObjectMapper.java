package org.aksw.jena_sparql_api.beans.json;

import com.google.common.base.Function;
import com.google.gson.JsonObject;

/**
 * Function that maps a JsonObject to another object
 *
 * @author raven
 *
 * @param <T>
 */
public interface JsonObjectMapper<T>
	extends Function<JsonObject, T>
{
}
