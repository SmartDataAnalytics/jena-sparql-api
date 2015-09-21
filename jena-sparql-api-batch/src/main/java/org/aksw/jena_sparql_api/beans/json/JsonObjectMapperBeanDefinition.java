package org.aksw.jena_sparql_api.beans.json;

import org.springframework.beans.factory.config.BeanDefinition;

import com.google.gson.JsonObject;

public class JsonObjectMapperBeanDefinition
	implements JsonObjectMapper<BeanDefinition>
{
	@Override
	public BeanDefinition apply(JsonObject json) {
		return null;
	}
}
