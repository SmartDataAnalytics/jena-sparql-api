package org.aksw.jena_sparql_api.sparql.ext.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.NodeValueVisitor;
import org.apache.jena.vocabulary.XSD;

public class NodeValueJson
    extends NodeValue
{
    private JsonElement json;

    public NodeValueJson(JsonElement json) {
        super();
        this.json = json;
    }

    public JsonElement getJson() {
        return json;
    }

    @Override
    protected Node makeNode() {
        RDFDatatype datatype = TypeMapper.getInstance().getSafeTypeByName(XSD.getURI() + "json");
        String str = datatype.unparse(json);
        Node result = NodeFactory.createLiteral(str, null, datatype);

        return result;
    }

    @Override
    public void visit(NodeValueVisitor arg0) {
        throw new UnsupportedOperationException();
    }

    public static NodeValueJson create(JsonElement obj) {
        NodeValueJson result = new NodeValueJson(obj);
        return result;
    }

    public static NodeValueJson create(String jsonStr) {
        Gson gson = new Gson();
        JsonElement e = gson.fromJson(jsonStr, JsonElement.class);
        NodeValueJson result = create(e);
        return result;
    }
}