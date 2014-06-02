package org.aksw.jena_sparql_api.core.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aksw.commons.collections.IClosable;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.ResultSetClosable;
import org.aksw.jena_sparql_api.utils.CannedQueryUtils;
import org.apache.jena.atlas.lib.Sink;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.syntax.Element;


public class QueryExecutionUtils {
	public static final Var vg = Var.alloc("g");
	public static final Var vs = Var.alloc("s");
	public static final Var vp = Var.alloc("p");
	public static final Var vo = Var.alloc("o");
	
	
	
	public static Iterator<Quad> createIteratorDumpQuads(QueryExecutionFactory qef) {
		String queryStr = "Select ?g ?s ?p ?o { Graph ?g { ?s ?p ?o } }";
		final QueryExecution qe = qef.createQueryExecution(queryStr);
		ResultSet tmp = qe.execSelect();

		ResultSetClosable rs = new ResultSetClosable(tmp, new IClosable() {
			@Override
			public void close() {
				qe.close();
			}
		}); 

		Iterator<Quad> result = new IteratorNQuads(rs);
		return result;
	}

	public static void createDumpNQuads(QueryExecutionFactory qef, Sink<Quad> sink) {
		Iterator<Quad> it = createIteratorDumpQuads(qef);
		while(it.hasNext()) {
			Quad quad = it.next();
			sink.send(quad);
		}		
	}

	public static Set<Quad> createDumpNQuads(QueryExecutionFactory qef) {
		SinkQuadsToCollection<? extends Set<Quad>> sink = SinkQuadsToCollection.createSinkHashSet();
		createDumpNQuads(qef, sink);
		Set<Quad> result = sink.getQuads();
		return result;
	}

	
	
	public static Iterator<Triple> createIteratorDumpTriples(QueryExecutionFactory qef) {
		//Query query = CannedQueryUtils.spoTemplate();
		String queryStr = "Construct { ?s ?p ?o } { ?s ?p ?o }";
		QueryExecution qe = qef.createQueryExecution(queryStr);
		Iterator<Triple> result = qe.execConstructTriples();
		return result;
	}
	
    public static long countQuery(Query query, QueryExecutionFactory qef) {
        Var outputVar = Var.alloc("_c_");
        
        if(query.isConstructType()) {
            
            Element element = query.getQueryPattern();
            query = new Query();
            query.setQuerySelectType();
            query.setQueryResultStar(true);
            query.setQueryPattern(element);
        }
        
        Query countQuery = QueryFactory.create("Select (Count(*) As ?_c_) { {" + query + "} }", Syntax.syntaxSPARQL_11);
        
        
        QueryExecution qe = qef.createQueryExecution(countQuery);
        ResultSet rs = qe.execSelect();
        Binding binding = rs.nextBinding();
        Node node = binding.get(outputVar);
        Number numeric = (Number)node.getLiteralValue();
        long result = numeric.longValue();
        

        return result;
    }
    
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
