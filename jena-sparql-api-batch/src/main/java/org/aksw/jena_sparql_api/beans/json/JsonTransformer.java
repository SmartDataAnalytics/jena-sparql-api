package org.aksw.jena_sparql_api.beans.json;

import com.google.common.base.Function;
import com.google.gson.JsonElement;

public interface JsonTransformer<T>
	extends Function<JsonElement, T>
{
}
