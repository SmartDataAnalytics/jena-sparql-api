package org.aksw.jena_sparql_api.sparql.ext.json;

import java.util.List;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

//JsonArray r = new JsonArray();
//Arrays.asList(Long.toBinaryString(n).split("")).stream()
//	.map(Integer::parseInt)
//	.forEach(r::add);
//
//Node node = NodeFactory.createLiteralByValue(r, jsonDatatype);
//result = NodeValue.makeNode(node);

public class E_JsonStrSplit
	extends FunctionBase
{
	@Override
	public NodeValue exec(List<NodeValue> args) {
		NodeValue result = null;

		NodeValue haystack = args.get(0);
		NodeValue needle = args.get(1);
		
		NodeValue limit = args.size() > 2 ? args.get(2) : null;

		int l = limit == null || limit.isInteger() ? -1 : limit.getInteger().intValue();
		
		if(haystack.isString() && needle.isString()) {
			RDFDatatype jsonDatatype = TypeMapper.getInstance().getTypeByClass(JsonElement.class);

			String h = haystack.getString();
			String n = needle.getString();
			
			String[] strs = h.split(n, l);
			
			JsonArray arr = new JsonArray();
			for(String str : strs) {
				arr.add(str);
			}
		
			Node node = NodeFactory.createLiteralByValue(arr, jsonDatatype);
			result = NodeValue.makeNode(node);
		}
		
		if (result == null) {
			throw new ExprEvalException("could not split a string based on given arguments");
		}

		return result;
	}

	@Override
	public void checkBuild(String uri, ExprList args) {
		// strSplit(?str, "pattern" [, limit])
		int n = args.size();
		if(n != 2 && n != 3) {
			throw new RuntimeException("strSplit expects 2 or 3 args");
		}
	}

}
