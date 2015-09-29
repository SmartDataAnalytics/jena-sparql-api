package org.aksw.jena_sparql_api.beans.json;

import org.aksw.gson.utils.JsonProcessor;
import org.aksw.spring.json.ContextProcessorJsonUtils;
import org.springframework.context.ApplicationContext;

import com.google.gson.JsonElement;


public class JsonProcessorContext
    implements JsonProcessor
{
    private ApplicationContext applicationContext;

    public JsonProcessorContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void process(JsonElement json) {
        try {
            ContextProcessorJsonUtils.processContext(applicationContext, json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
