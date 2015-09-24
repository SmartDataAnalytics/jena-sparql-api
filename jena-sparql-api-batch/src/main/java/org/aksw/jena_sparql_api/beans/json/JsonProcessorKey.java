package org.aksw.jena_sparql_api.beans.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Applies the given json processors to each key in a json object
 *
 * @author raven
 *
 */
public class JsonProcessorKey
	implements JsonProcessor
{
	private List<JsonProcessor> processors = new ArrayList<JsonProcessor>();

	public void process(JsonElement json) {
		if(json.isJsonObject()) {
			JsonObject obj = json.getAsJsonObject();
			for(Entry<String, JsonElement> entry : obj.entrySet()) {
				JsonElement val = entry.getValue();

				for(JsonProcessor processor : processors) {
					processor.process(val);
				}
			}
		}
	}

	public List<JsonProcessor> getProcessors() {
		return processors;
	}
}
