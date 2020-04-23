package org.aksw.jena_sparql_api.io.json;

import java.io.IOException;
import java.util.function.Supplier;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class TypeAdapterDataset
    extends TypeAdapter<Dataset>
{
    protected Supplier<Dataset> datasetSupplier;

    public TypeAdapterDataset() {
        this(DatasetFactory::create);
    }

    public TypeAdapterDataset(Supplier<Dataset> datasetSupplier) {
        super();
        this.datasetSupplier = datasetSupplier;
    }

    @Override
    public void write(JsonWriter out, Dataset value) throws IOException {
        JsonObject obj = RDFNodeJsonUtils.toJsonObject(value, new Gson());

        // We write out the whole JSON in a string because otherwise the parsing becomes a pain
        // The resulting json is ugly though
        out.value("" + obj);
    }

    @Override
    public Dataset read(JsonReader in) throws IOException {
        //RDFNodeJsonUtils.toDataset(str);
        String str = in.nextString();
        Dataset result = datasetSupplier.get();
        RDFNodeJsonUtils.toDataset(result, str);
        return result;
    }
}