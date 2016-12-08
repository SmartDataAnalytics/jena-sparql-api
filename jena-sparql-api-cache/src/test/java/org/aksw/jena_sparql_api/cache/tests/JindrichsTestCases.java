package org.aksw.jena_sparql_api.cache.tests;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.optimize.TransformPathFlatternStd;
import org.junit.Assert;
import org.junit.Test;

public class JindrichsTestCases {

// From the mail from 4.8.2016
//	- FILTER (?a = ?b) should be equal to FILTER (?b = ?a)
//	- FILTER sameTerm(?a, ?b) should be equal to FILTER sameTerm(?b, ?a)
//	- FILTER (?a && ?b) should be equal to FILTER (?b && ?a)
//	- [] a ?a . FILTER NOT EXISTS { ?a a [] . } should be equal to [] a ?b . OPTIONAL { ?a a ?class . } FILTER (!bound(?class)).
//	- { [] a ?a . } UNION { [] a ?b . } should be equal to { [] a ?b . } UNION { [] a ?a . }
//	- { [] :p1 [] . } UNION { [] :p2 [] . } should be equal to VALUES ?p { :p1 :p2 } [] ?p [] .
//	- { [] :p1 [] . } UNION { [] :p2 [] . } should be equal to [] :p1|:p2 [] .

//	- FILTER (?a && ?b) should be equal to FILTER (?b && ?a)
	
	@Test
	public void test8() {
		Query expected = QueryFactory.create("PREFIX : <http://ex.org/> SELECT * { { ?s :p1 ?o } UNION { ?s :p2 ?o } }");

    	Op op = Algebra.compile(QueryFactory.create("PREFIX : <http://ex.org/> SELECT * { ?s :p1|:p2 ?o }"));
    	op = Transformer.transform(new TransformPathFlatternStd(), op);
    	Query actual = OpAsQuery.asQuery(op);
    	actual.setPrefix("", "http://ex.org/");

//    	System.out.println(expected);
//    	System.out.println(actual);

    	// TODO Figure out why the strings but not the query objects are equal
    	Assert.assertEquals("" + expected, "" + actual);
	}
}
