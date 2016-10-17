package org.aksw.jena_sparql_api.algebra.transform;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.junit.Test;

public class OpTransformTests {

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
