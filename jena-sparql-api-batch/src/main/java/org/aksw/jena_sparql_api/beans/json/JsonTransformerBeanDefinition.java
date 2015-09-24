package org.aksw.jena_sparql_api.beans.json;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import com.google.gson.JsonObject;

public class JsonTransformerBeanDefinition
	extends JsonTransformerObject
{
	private BeanDefinitionRegistry beanDefinitionRegistry;

	public JsonTransformerBeanDefinition(BeanDefinitionRegistry beanDefinitionRegistry) {
		this.beanDefinitionRegistry = beanDefinitionRegistry;
	}

	@Override
	public Object apply(JsonObject json) {
        Object result;

        if(ContextProcessorJsonUtils.isRef(json)) {
			result = ContextProcessorJsonUtils.getAsRef(json);
//		} else if(ContextProcessorJsonUtils.isBean(json)) {

        } else {
        	result = super.apply(json);
        }

        return result;
	}

}
