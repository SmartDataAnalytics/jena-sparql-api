package org.aksw.jena_sparql_api.beans.json;

import org.aksw.gson.utils.JsonTransformerObject;
import org.aksw.spring.json.ContextProcessorJsonUtils;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
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
	public Object visit(JsonObject json) {
        Object result;

        if(ContextProcessorJsonUtils.isRef(json)) {
			result = ContextProcessorJsonUtils.getAsRef(json);
		} else if(json.has("autowired")) {
			String autowiredType = json.get("autowired").getAsString();
			if(autowiredType.equals("byType")) {
				result = new AutowireCandidateQualifier(Object.class);
			} else {
				throw new RuntimeException("Unknow autowire type: " + autowiredType);
			}
        } else {
        	result = super.apply(json);
        }

        return result;
	}

}
