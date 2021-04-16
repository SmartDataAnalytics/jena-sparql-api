package org.aksw.jena_sparql_api.sparql.ext.json;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class E_JsonReverse
	extends FunctionBase1
{

	@Override
	public NodeValue exec(NodeValue v) {
		NodeValue result = null;

		Node tmp = v.asNode();
		if(tmp.isLiteral()) {
			RDFDatatype dtype = tmp.getLiteralDatatype();
			if(dtype.getJavaClass().isAssignableFrom(JsonElement.class)) {
				JsonElement el = (JsonElement)tmp.getLiteralValue();
				
				if(el.isJsonArray()) {
					JsonArray arr = el.getAsJsonArray();
					JsonArray t = new JsonArray();
				
					for(int i = arr.size() -1; i >= 0; --i) {
						JsonElement item = arr.get(i);
						t.add(item);
					}

					result = NodeValue.makeNode(NodeFactory.createLiteralByValue(t, dtype));
				}			
			}
		}
		
		if (result == null) {
			throw new ExprEvalException("argument failed to parse as json");
		}

		return result;
	}
}
