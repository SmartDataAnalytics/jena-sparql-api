package org.aksw.jena_sparql_api.io.json;

import java.io.IOException;

import org.apache.jena.query.Dataset;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class TypeAdapterDataset
	extends TypeAdapter<Dataset>
{
	@Override
	public void write(JsonWriter out, Dataset value) throws IOException {
		JsonObject obj = RDFNodeJsonUtils.toJsonObject(value, new Gson());

		out.value("" + obj);
	}
	
	@Override
	public Dataset read(JsonReader in) throws IOException {		
		String str = in.nextString();
		Dataset result = RDFNodeJsonUtils.toDataset(str);
		return result;
	}
}