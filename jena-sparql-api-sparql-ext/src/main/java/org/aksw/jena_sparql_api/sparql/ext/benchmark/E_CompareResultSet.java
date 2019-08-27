package org.aksw.jena_sparql_api.sparql.ext.benchmark;

import java.io.ByteArrayInputStream;

import org.aksw.jena_sparql_api.sparql.ext.json.E_JsonPath;
import org.aksw.jena_sparql_api.utils.ResultSetUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class E_CompareResultSet
	extends FunctionBase2
{
	private static final Logger logger = LoggerFactory.getLogger(E_CompareResultSet.class);
	

	/**
	 * v1 = expected
	 * v2 = actual
	 */
	@Override
	public NodeValue exec(NodeValue v1, NodeValue v2) {
		NodeValue result;
		
		Node n1 = v1.asNode();
		Node n2 = v2.asNode();

		Object o1 = n1.getLiteralValue();
		Object o2 = n2.getLiteralValue();

		JsonElement j1 = o1 instanceof JsonElement ? (JsonElement)o1 : null;
		JsonElement j2 = o2 instanceof JsonElement ? (JsonElement)o2 : null;

		if(j1 != null && j2 != null) {
			String s1 = j1.toString();
			String s2 = j2.toString();
			
			ResultSet r1 = ResultSetFactory.fromJSON(new ByteArrayInputStream(s1.getBytes()));
			ResultSet r2 = ResultSetFactory.fromJSON(new ByteArrayInputStream(s2.getBytes()));
			
	        Multiset<Binding> m1 = ResultSetUtils.toMultiset(r1);
	        Multiset<Binding> m2 = ResultSetUtils.toMultiset(r2);
	        Multiset<Binding> both = Multisets.intersection(m1, m2);

	        int m1s = m1.size();
	        int m2s = m2.size();
	        int boths = both.size();
	        
	        double precision = boths / (double)m2s;
	        double recall = boths / (double)m1s;
	        double fmeasure = 2.0 * (precision * recall) / (precision + recall);

	        JsonObject json = new JsonObject();
	        json.addProperty("precision", precision);
	        json.addProperty("recall", recall);
	        json.addProperty("fmeasure", fmeasure);
	        json.addProperty("expectedSize", m1s);
	        json.addProperty("actualSize", m2s);
	        
	        result = NodeValue.makeNode(E_JsonPath.jsonToNode(json));
			
		} else {
			throw new ExprEvalException("At least one of the arguments could not be parsed as a SPARQL result set");
		}
		
		// TODO Auto-generated method stub
		return result;
	}
}
