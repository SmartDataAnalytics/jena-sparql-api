package org.aksw.jena_sparql_api.beans.json;

import org.springframework.context.ApplicationContext;

public class ContextProcessorJsonImpl
    implements ContextProcessorJson
{
    private ApplicationContext applicationContext;


    @Override
    public void processContext(Object ctx) {
        ContextProcessorJsonUtils.processContext(c, context, classAliasMap);
        // TODO Auto-generated method stub
    }
    
    
}
