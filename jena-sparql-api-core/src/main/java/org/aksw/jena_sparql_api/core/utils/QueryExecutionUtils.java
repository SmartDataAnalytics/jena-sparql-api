package org.aksw.jena_sparql_api.core.utils;

import java.util.HashSet;
import java.util.Set;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

public class QueryExecutionUtils {
	public static final Var vg = Var.alloc("g");
	public static final Var vs = Var.alloc("s");
	public static final Var vp = Var.alloc("p");
	public static final Var vo = Var.alloc("o");
	
	
	public static Set<Quad> createDumpNQuads(QueryExecutionFactory qef) {
		String queryStr = "Select ?g ?s ?p ?o { Graph ?g { ?s ?p ?o } }";
		QueryExecution qe = qef.createQueryExecution(queryStr);
		ResultSet rs = qe.execSelect();
		
		/*
		Var vg = Var.alloc("g");
		Var vs = Var.alloc("s");
		Var vp = Var.alloc("p");
		Var vo = Var.alloc("o");
		*/
		
		Set<Quad> result = new HashSet<Quad>();
		while(rs.hasNext()) {
			Binding binding = rs.nextBinding();
//				Node g = getNode(binding, vg, Quad.defaultGraphNodeGenerated);
//				Node s = getNode(binding, vs, Quad.defaultGraphNodeGenerated);
//				Node p = getNode(binding, vp, Quad.defaultGraphNodeGenerated);
//				Node o = getNode(binding, vo, Quad.defaultGraphNodeGenerated);

			Node g = binding.get(vg);
			Node s = binding.get(vs);
			Node p = binding.get(vp);
			Node o = binding.get(vo);
			
			Quad quad = new Quad(g, s, p, o);
			result.add(quad);
		}
		
		return result;
	}
}
