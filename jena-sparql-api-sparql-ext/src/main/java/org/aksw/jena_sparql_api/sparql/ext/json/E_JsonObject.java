package org.aksw.jena_sparql_api.sparql.ext.json;

import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase;
import org.apache.jena.sparql.sse.builders.ExprBuildException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class E_JsonObject
    extends FunctionBase
{

    @Override
    public NodeValue exec(List<NodeValue> args) {
        int argCount = args.size();
        int pairCount = argCount >> 1; // Divide by 2

        JsonObject jsonObject = new JsonObject();
        for (int i = 0; i < pairCount; ++i) {
            int offset = i << 1;
            NodeValue key = args.get(offset);
            NodeValue value = args.get(offset + 1);

            // Ignoring null keys (behavior may change)
            if (key != null) {
                String jsonKey = key.isString()
                        ? key.asString()
                        : key.isIRI()
                            ? key.asNode().getURI()
                            : null;

                if (jsonKey == null) {
                    throw new ExprEvalException("Non string key in json object");
                }

                Node v = value == null ? null : value.asNode();
                JsonElement jsonValue = E_JsonConvert.convert(v, RDFDatatypeJson.INSTANCE.getGson());

                jsonObject.add(jsonKey, jsonValue);
            }
        }

        NodeValue result = RDFDatatypeJson.jsonToNodeValue(jsonObject);
        return result;
    }

    @Override
    public void checkBuild(String uri, ExprList args) {
        if (args.size() % 2 != 0) {
            throw new ExprBuildException("Json object creation requires an even number of arguments (every two arguments are interpreted as a key-value pair");
        }
    }

}
