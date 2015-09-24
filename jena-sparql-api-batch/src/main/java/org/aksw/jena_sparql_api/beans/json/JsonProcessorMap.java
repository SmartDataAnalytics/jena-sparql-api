package org.aksw.jena_sparql_api.beans.json;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonProcessorMap
	implements JsonProcessor
{
	//private List<Entry<String, JsonProcessor>> keyToProcessor;
	// NOTE Is LinkedListMultimap<K, V> an option?
	private List<JsonProcessorDefinition> procDefs = new ArrayList<JsonProcessorDefinition>();

	@Override
	public void process(JsonElement json) {
		if(json.isJsonObject()) {
			JsonObject obj = json.getAsJsonObject();

			for(JsonProcessorDefinition procDef : procDefs) {
				String key = procDef.getKey();
				boolean isMandatory = procDef.isMandatory();

				JsonElement child = obj.get(key);
				boolean isNullChild = child == null || (child.isJsonPrimitive() && child.getAsJsonPrimitive().isJsonNull());
				if(isNullChild) {
					if(isMandatory) {
						throw new RuntimeException("Processor mandated value for key " + key);
					}
				} else {
					JsonProcessor proc = procDef.getJsonProcessor();
					proc.process(child);
				}
			}
		}
	}

	public void register(String key, boolean isMandatory, JsonProcessor jsonProcessor) {
		JsonProcessorDefinition procDef = new JsonProcessorDefinition(key, jsonProcessor, isMandatory);
		register(procDef);
	}

	public void register(JsonProcessorDefinition procDef) {
		procDefs.add(procDef);
	}
}
