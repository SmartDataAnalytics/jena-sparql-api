package org.aksw.jena_sparql_api.beans.json;

public class JsonProcessorDefinition {
	private String key;
	private JsonProcessor jsonProcessor;
	private boolean isMandatory;

	public JsonProcessorDefinition(String key, JsonProcessor jsonProcessor, boolean isMandatory) {
		super();
		this.key = key;
		this.jsonProcessor = jsonProcessor;
		this.isMandatory = isMandatory;
	}

	public boolean isMandatory() {
		return isMandatory;
	}

	public String getKey() {
		return key;
	}

	public JsonProcessor getJsonProcessor() {
		return jsonProcessor;
	}
}
