package org.aksw.jena_sparql_api.beans.json;

import com.google.gson.JsonElement;

public interface JsonProcessor {
	void process(JsonElement json);
}
