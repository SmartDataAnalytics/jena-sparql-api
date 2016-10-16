package org.aksw.jena_sparql_api.cache.tests;

import java.util.LinkedHashMap;
import java.util.Map;

import org.aksw.jena_sparql_api.concept_cache.op.OpUtils;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
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
		QueryExecution qe = qef.createQueryExecution(q);
		Binding result = qe.execSelect().nextBinding();
		qe.close();
		return result;
	}

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
