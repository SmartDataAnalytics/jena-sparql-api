package org.aksw.jena_sparql_api.sparql.ext.json;

import org.apache.commons.lang.NotImplementedException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueVisitor;
import com.hp.hpl.jena.vocabulary.XSD;

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
        throw new NotImplementedException();
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