package org.aksw.jena_sparql_api.utils.model;

import java.util.Iterator;
import java.util.List;

import org.aksw.jena_sparql_api.rdf.collections.ListFromRDFList;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.vocabulary.RDF;
import org.junit.Test;

public class TestListViewFromRDFList {

	/**
	 * Simply compare the list operations of the view with that of a reference
	 * 
	 * - we can use the proxy util from the subgraphisomorphism package
	 * 
	 */
	@Test
	public void testListViewFromRDFList() {

		Model x = ModelFactory.createDefaultModel();
		Resource r = x.createResource();
		List<RDFNode> javaList = new ListFromRDFList(r, RDF.type);
		
		javaList.add(x.asRDFNode(NodeValue.makeInteger(10).asNode()));
		javaList.add(x.createLiteral("hi"));
		javaList.add(x.createResource("http://www.example.org/"));
		
		System.out.println(javaList);

		System.out.println(javaList.get(1));

		// FIXME Turn into unit test
		
		// Remove middle elment
		{
			Iterator<?> it = javaList.iterator();
			it.next();
			it.remove();
			System.out.println(javaList);
		}
		

		// Remove first element
		{
			Iterator<?> it = javaList.iterator();
			it.next();
			it.remove();
			System.out.println(javaList);
		}
		
		// Prepend element
		{
			System.out.println("Prepend element to " + javaList);
			javaList.add(0, x.createLiteral("first"));
			System.out.println(javaList);
		}
		
		
		// Append element
		{
			javaList.add(2, x.createLiteral("last"));
			System.out.println(javaList);
		}

		
		// Remove last
		{
			Iterator<?> it = javaList.iterator();
			it.next();
			it.remove();
			it.next();
			it.remove();
			it.next();
			it.remove();
			System.out.println(javaList);
		}
		
		
		// Clear

		System.out.println("RESULT");
		RDFDataMgr.write(System.out, x, RDFFormat.TURTLE_PRETTY);

	}
}
