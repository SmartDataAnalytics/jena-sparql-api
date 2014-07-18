package org.aksw.jena_sparql_api.gson;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class TypeAdapterNoop<T>
    extends TypeAdapter<T>
{
    @Override
    public void write(JsonWriter out, T value) throws IOException {
        out.nullValue();
    }

    @Override
    public T read(JsonReader in) throws IOException {
        return null;
    }
}