package org.aksw.jena_sparql_api.sparql.ext.json;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

public class E_BinaryString
	extends FunctionBase1
{

	@Override
	public NodeValue exec(NodeValue v) {
		NodeValue result = null;

		Node tmp = v.asNode();
		if (tmp.isLiteral()) {
			Object o = tmp.getLiteralValue();
			if(o instanceof Number) {
				
				long n = ((Number)o).longValue();
				result = NodeValue.makeString(Long.toBinaryString(n));				
			}
		}
		
		if (result == null) {
			throw new ExprEvalException("argument not a numeric literal");
		}

		return result;
	}


}
