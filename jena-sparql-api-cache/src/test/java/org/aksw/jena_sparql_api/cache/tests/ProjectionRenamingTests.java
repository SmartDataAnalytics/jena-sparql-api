package org.aksw.jena_sparql_api.cache.tests;

import java.util.LinkedHashMap;
import java.util.Map;

import org.aksw.jena_sparql_api.algebra.utils.OpUtils;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingHashMap;
import org.junit.Assert;
import org.junit.Test;

public class ProjectionRenamingTests {
	public static final Node s = NodeFactory.createURI("http://ex.org/s");
	public static final Node p = NodeFactory.createURI("http://ex.org/p");
	public static final Node o = NodeFactory.createURI("http://ex.org/o");


	public static Binding eval(Op op) {
		Query q = OpAsQuery.asQuery(op);
		QueryExecutionFactory qef = FluentQueryExecutionFactory.from(ModelFactory.createDefaultModel()).create();
		Binding result;
		try(QueryExecution qe = qef.createQueryExecution(q)) {
			result = qe.execSelect().nextBinding();
		}
		return result;
	}

//	@Test
//	public void testVarRenamingJena3_15_0_a() {
//		Query q = OpAsQuery.asQuery(Algebra.compile(QueryFactory.create(
//				"SELECT (?s as ?x) { BIND(?y AS ?s) }")));
//		
//		try(QueryExecution qe = org.apache.jena.query.QueryExecutionFactory.create(q, ModelFactory.createDefaultModel())) {
//			System.out.println("" + ResultSetFormatter.asText(qe.execSelect()));
//		}
//		System.out.println(q);
//	}

	
	/*
(project (?x ?o ?z)
  (extend ((?x ?s) ?o (?z ?p)) <-- The o is suspicios
    (table (vars ?s ?p ?o)
      (row [?p <http://ex.org/p>] [?o <http://ex.org/o>] [?s <http://ex.org/s>])
    )))

	 */
//	@Test
//	public void testVarRenamingJena3_15_0_b() {
//		String str = "SELECT ?x ?o ?z { VALUES (?s ?p ?o) { (<urn:s> <urn:p> <urn:o>) } BIND(?s AS ?x) BIND(?p AS ?z) BIND(?o AS ?o) }";
//		Query qa = QueryFactory.create(str);
//		Op op = Algebra.compile(qa);
//		System.out.println(op);
//		Query qb = OpAsQuery.asQuery(op);
//		
//		try(QueryExecution qe = org.apache.jena.query.QueryExecutionFactory.create(qb, ModelFactory.createDefaultModel())) {
//			System.out.println("" + ResultSetFormatter.asText(qe.execSelect()));
//		}
//		System.out.println(qb);
//	}

	
	@Test
	public void testVarRenamingSimple() {
		Op op = Algebra.compile(QueryFactory.create(
				"PREFIX ex: <http://ex.org/> SELECT * { VALUES (?s ?p ?o) { (ex:s ex:p ex:o) } }"));

		Map<Var, Var> varMap = new LinkedHashMap<>();
		varMap.put(Vars.s, Vars.x);
		varMap.put(Vars.o, Vars.o);
		varMap.put(Vars.p, Vars.z);
		Op renamed = OpUtils.wrapWithProjection(op, varMap);

		Binding actual = eval(renamed);

		BindingHashMap expected = new BindingHashMap();
		expected.add(Vars.x, s);
		expected.add(Vars.o, o);
		expected.add(Vars.z, p);

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testVarRenamingComplex() {
		Op op = Algebra.compile(QueryFactory.create("PREFIX ex: <http://ex.org/> SELECT * { VALUES (?s ?p ?o) { (ex:s ex:p ex:o) } }"));
		Map<Var, Var> varMap = new LinkedHashMap<>();
		varMap.put(Vars.s, Vars.o);
		varMap.put(Vars.o, Vars.s);
		varMap.put(Vars.p, Vars.p);
		Op renamed = OpUtils.wrapWithProjection(op, varMap);

		Binding actual = eval(renamed);

		BindingHashMap expected = new BindingHashMap();
		expected.add(Vars.o, s);
		expected.add(Vars.s, o);
		expected.add(Vars.p, p);

		Assert.assertEquals(expected, actual);
	}
}
