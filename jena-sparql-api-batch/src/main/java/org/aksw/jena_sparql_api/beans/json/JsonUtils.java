package org.aksw.jena_sparql_api.beans.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class JsonUtils {
    public static JsonElement safeGet(JsonArray arr, int i) {
        JsonElement result = i < arr.size() ? arr.get(i) : null;
        return result;
    }

    public static Object safeGetObject(JsonArray arr, int i) {
        JsonElement tmp = safeGet(arr, i);
        Object result = JsonTransformerUtils.toJavaObject(tmp);
        return result;
    }
}
