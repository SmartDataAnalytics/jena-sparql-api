package org.aksw.jena_sparql_api.beans.json;

import org.springframework.context.ApplicationContext;

import com.google.gson.JsonElement;

public class ContextProcessorJsonImpl
    implements ContextProcessorJson
{
    private ApplicationContext applicationContext;

    public ContextProcessorJsonImpl(ApplicationContext applicationContext) {
    	this.applicationContext = applicationContext;
    }

    @Override
    public void processContext(JsonElement ctx) {
        //ContextProcessorJsonUtils.processContext(c, context, classAliasMap);
        // TODO Auto-generated method stub
    }


}
