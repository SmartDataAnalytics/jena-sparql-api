package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

public class F_Wkb2Wkt
	extends FunctionBase1
{
	@Override
	public NodeValue exec(NodeValue v) {		
		GeometryWrapper gw = GeometryWrapper.extract(v);
		Node node = NodeFactory.createLiteralByValue(gw, WKTDatatype.INSTANCE);
		NodeValue result = NodeValue.makeNode(node);
		return result;
	}
}
