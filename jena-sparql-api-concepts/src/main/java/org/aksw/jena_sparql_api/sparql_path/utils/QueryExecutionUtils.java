package org.aksw.jena_sparql_api.sparql_path.utils;

import java.util.ArrayList;
import java.util.List;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

public class QueryExecutionUtils {
	public static List<Node> executeList(QueryExecutionFactory qef, Query query) {
		List<Node> result = new ArrayList<Node>();
		
		List<Var> vars = query.getProjectVars();
		if(vars.size() != 1) {
			throw new RuntimeException("Exactly 1 var expected");
		}
		
		Var var = vars.get(0);
		
		QueryExecution qe = qef.createQueryExecution(query);
		ResultSet rs = qe.execSelect();
		while(rs.hasNext()) {
			//QuerySolutiors.next()
			Binding binding = rs.nextBinding();
			Node node = binding.get(var);
			
			result.add(node);
		}
		
		return result;
	}

}
