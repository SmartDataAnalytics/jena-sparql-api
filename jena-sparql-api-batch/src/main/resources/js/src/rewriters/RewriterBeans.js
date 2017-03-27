rewriters.push(function(json) {
  var result = json;
  

});


    public static JsonElement expandJava(Gson gson, JsonElement json) {
        JsonElement result = json.isJsonNull()
            ? json
            : expandObjectJava(gson, json);

        return result;
    }

    public static JsonObject expandObjectJava(Gson gson, JsonElement json) {
        JsonObject result;

        if(json.isJsonObject()) {
            result = json.getAsJsonObject();
        } else if(json.isJsonArray()) {
            result = expandJavaArray(gson, json.getAsJsonArray());
        } else if(json.isJsonPrimitive()) {
            result = expandJavaPrimitive(gson, json.getAsJsonPrimitive());
        } else {
            throw new RuntimeException("Should not happen");
        }

        return result;
    }

    public static JsonObject expandJavaArray(Gson gson, JsonArray json) {
        JsonObject result = new JsonObject();
        result.addProperty("type", "org.springframework.beans.factory.support.ManagedList");
        result.add("ctor", json);

        return result;
    }

    public static JsonObject expandJavaPrimitive(Gson gson, JsonPrimitive json) {
        Object val = gson.fromJson(json, Object.class);
        Class<?> clazz = val.getClass();

        JsonArray ctorArgs = new JsonArray();
        ctorArgs.add(json);

        JsonObject result = new JsonObject();
        result.addProperty("type", clazz.getName());
        result.add("ctor", ctorArgs);

        return result;
    }


    public static JsonElement processValue(Gson gson, JsonElement json, String qualifier) {
        JsonElement result;
        if(!StringUtils.isEmpty(qualifier) && !(json.isJsonObject() && json.getAsJsonObject().has("qualifier"))) {
            JsonElement tmp = expandJava(gson, json);
            if(tmp.isJsonObject()) {
                JsonObject o = tmp.getAsJsonObject();
                o.addProperty("qualifier", qualifier);
            }

            result = tmp;
        } else {
            result = json;
        }

        return result;
    }

    @Override
    public JsonElement visit(JsonObject json) {
        JsonObject tmp = new JsonObject();


        // Iterate the keys and check for qualifiers
        boolean isChange = false;
        for(Entry<String, JsonElement> entry : json.entrySet()) {
            String key = entry.getKey();
            Entry<String, String> keyAndQualifier = ContextProcessorJsonUtils.splitByQualifier(key);

            String qualifier = keyAndQualifier.getValue();
            JsonElement value = entry.getValue();

            JsonElement newValue = processValue(gson, value, qualifier);
            isChange = isChange || !value.equals(newValue);

            tmp.add(key, newValue);
        }

        JsonObject result = isChange ? tmp : json;

        return result;
    }
}