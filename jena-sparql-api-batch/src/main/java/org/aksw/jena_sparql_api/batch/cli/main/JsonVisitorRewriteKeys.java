package org.aksw.jena_sparql_api.batch.cli.main;

import java.util.Map.Entry;

import org.aksw.gson.utils.JsonVisitorRewrite;

import com.google.common.base.Function;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonVisitorRewriteKeys
    extends JsonVisitorRewrite
{
    protected Function<JsonElement, JsonElement> subRewriter;

    public JsonVisitorRewriteKeys(Function<JsonElement, JsonElement> subRewriter) {
        this.subRewriter = subRewriter;
    }

    @Override
    public JsonObject visit(JsonObject json) {
        JsonObject result = new JsonObject();
        for(Entry<String, JsonElement> entry : json.entrySet()) {
            String key = entry.getKey();
            JsonElement before = entry.getValue();
            JsonElement after = subRewriter.apply(before);

            result.add(key, after);
        }

        return result;
    }

    public static JsonVisitorRewriteKeys create(Function<JsonElement, JsonElement> subRewriter) {
        JsonVisitorRewriteKeys result = new JsonVisitorRewriteKeys(subRewriter);
        return result;
    }
}