package org.aksw.jena_sparql_api.batch.step;

import org.springframework.beans.factory.FactoryBean;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class FactoryBeanJsonElement
	implements FactoryBean<JsonElement>
{
	private Gson gson;
	private String jsonStr;

	public FactoryBeanJsonElement() {
		this(new Gson());
	}

	public FactoryBeanJsonElement(Gson gson) {
		this.gson = gson;
	}

	public String getJsonStr() {
		return jsonStr;
	}


	public void setJsonStr(String jsonStr) {
		this.jsonStr = jsonStr;
	}


	@Override
	public JsonElement getObject() throws Exception {
		JsonElement result = gson.fromJson(jsonStr, JsonElement.class);
		return result;
	}

	@Override
	public Class<?> getObjectType() {
		return JsonElement.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}
}
