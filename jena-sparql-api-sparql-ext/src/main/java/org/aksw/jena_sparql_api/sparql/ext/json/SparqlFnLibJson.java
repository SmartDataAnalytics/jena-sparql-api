package org.aksw.jena_sparql_api.sparql.ext.json;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;

import com.google.gson.JsonElement;

public class SparqlFnLibJson {

    /** Get the length of a json array. Raises {@link IllegalArgumentException} for non array arguments */
    @IriNs(JenaExtensionJson.ns)
    public static int length(JsonElement json) {
        int result;
        if (json.isJsonArray()) {
            result = json.getAsJsonArray().size();
        } else {
            throw new IllegalArgumentException("Argument is not a json array");
        }

        return result;
    }
}
