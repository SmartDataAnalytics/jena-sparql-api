package org.aksw.jena_sparql_api.io.json;

import java.io.IOException;

import org.aksw.jena_sparql_api.rdf.model.ext.dataset.api.NodesInDataset;
import org.aksw.jena_sparql_api.rdf.model.ext.dataset.impl.NodesInDatasetImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.vocabulary.RDF;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;


public class TypeAdapterNode
    extends TypeAdapter<Node>
{
    @Override
    public void write(JsonWriter out, Node value) throws IOException {
        String str = RDFNodeJsonUtils.nodeToStr(value);
        out.value(str);
    }

    @Override
    public Node read(JsonReader in) throws IOException {
        String str = in.nextString();
        Node result = RDFNodeJsonUtils.strToNode(str);
        return result;
    }

    public static void main(String[] args) {
        Gson gson = new GsonBuilder()
                .registerTypeHierarchyAdapter(Node.class, new TypeAdapterNode())
                .registerTypeHierarchyAdapter(Dataset.class, new TypeAdapterDataset())
                .create();


        NodesInDataset r = new NodesInDatasetImpl(DatasetFactory.create());
        r.getDataset().getNamedModel("http://foobar").add(RDF.type, RDF.type, RDF.Property);
        r.add("http://foobar", RDF.type.asNode());

        String str = gson.toJson(r);

        NodesInDataset roundtrip = gson.fromJson(str, NodesInDatasetImpl.class);

        System.out.println(str);
        System.out.println(roundtrip);
    }
}
