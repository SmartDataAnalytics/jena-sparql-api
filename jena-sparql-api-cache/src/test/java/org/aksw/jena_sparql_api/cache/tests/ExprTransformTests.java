package org.aksw.jena_sparql_api.cache.tests;

import org.aksw.jena_sparql_api.algebra.transform.ExprTransformVariableOrder;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.util.ExprUtils;
import org.junit.Assert;
import org.junit.Test;


public class ExprTransformTests {
	@Test
	public void testSymmetry() {
		test("1 + ?s", "?s + 1");
		test("?b + ?a", "?a + ?b");
	}

	public static void test(String actualStr, String expectedStr) {
		Expr actual = ExprTransformer.transform(new ExprTransformVariableOrder(), ExprUtils.parse(actualStr));
		Expr expected = ExprUtils.parse(expectedStr);
		Assert.assertEquals(expected, actual);
	}
}
