package org.aksw.jena_sparql_api.algebra.transform;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.optimize.Optimize;
import org.apache.jena.sparql.algebra.optimize.TransformMergeBGPs;
import org.junit.Assert;
import org.junit.Test;


public class OpTransformTests {

	@Test
	public void testDistributeJoinUnion() {
		Query expected = QueryFactory.create("PREFIX : <http://ex.org/> SELECT * { { :a :b :c . :x :y :z } UNION { :d :e :f . :x :y :z } }");

		Op op = Algebra.compile(
				QueryFactory.create("PREFIX : <http://ex.org/> SELECT * { { :a :b :c } UNION { :d :e :f } :x :y :z }"));

		op = TransformDistributeJoinOverUnion.transform(op);
		op = Optimize.apply(new TransformMergeBGPs(), op);
		Query actual = OpAsQuery.asQuery(op);
		actual.setPrefix("", "http://ex.org/");

		System.out.println(expected);
		System.out.println(actual);

		Assert.assertEquals(expected, actual);

	}

	@Test
	public void testBGPToFiltersRoundTrip() {
		Op op = Algebra.toQuadForm(Algebra.compile(
				QueryFactory.create("SELECT * { ?s a <http://ex.org/Airport> }")));

		System.out.println("a:" + op);
		op = TransformReplaceConstants.transform(op);
		System.out.println("b:" + op);
		op = TransformPushFiltersIntoBGP.transform(op);
		System.out.println("c:" + op);

		Query r = OpAsQuery.asQuery(op);
		System.out.println(r);
		//TransformPushFiltersIntoBGP(
	}
}
