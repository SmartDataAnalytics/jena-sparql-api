package org.aksw.jena_sparql_api.sparql.ext.json;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class E_JsonConvert
    extends FunctionBase1
{

    @Override
    public NodeValue exec(NodeValue v) {
        Node node = v == null ? null : v.asNode();
        JsonElement jsonElement = convert(node, RDFDatatypeJson.INSTANCE.getGson());
        NodeValue result = RDFDatatypeJson.jsonToNodeValue(jsonElement);
        return result;
    }

    /**
     * Convert the given node to a JsonElement.
     * IRIs are be converted to strings.
     *
     * If the node already is a json object (a literal value of type JsonElement) then the argument is returned.
     *
     * @param node
     * @param gson
     * @return
     */
    public static JsonElement convert(Node node, Gson gson) {
        Object javaObject;

        if (node == null) {
            javaObject = null;
        } else if (node.isURI()) {
            javaObject = node.getURI();
        } else if (node.isLiteral()) {
            javaObject = node.getLiteralValue();
        } else {
            // Unsupported rdf term type (e.g. Node_Triple / Blank node)
            javaObject = null;
        }

        JsonElement result = javaObject instanceof JsonElement
                ? (JsonElement)javaObject
                : gson.toJsonTree(javaObject);

        return result;
    }
}
