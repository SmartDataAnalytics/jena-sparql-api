package org.aksw.jena_sparql_api.beans.json;

import com.google.gson.JsonElement;

public interface ContextProcessorJson
{
    void processContext(JsonElement ctx);
}
