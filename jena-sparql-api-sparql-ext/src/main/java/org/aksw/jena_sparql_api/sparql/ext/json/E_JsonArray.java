package org.aksw.jena_sparql_api.sparql.ext.json;

import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class E_JsonArray
    extends FunctionBase
{
    @Override
    public NodeValue exec(List<NodeValue> args) {
        int argCount = args.size();

        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < argCount; ++i) {
            NodeValue nodeValue = args.get(i);
            Node node = nodeValue == null ? null : nodeValue.asNode();
            JsonElement jsonElement = E_JsonConvert.convert(node, RDFDatatypeJson.INSTANCE.getGson());
            jsonArray.add(jsonElement);
        }

        NodeValue result = RDFDatatypeJson.jsonToNodeValue(jsonArray);
        return result;
    }

    @Override
    public void checkBuild(String uri, ExprList args) {
    }
}
