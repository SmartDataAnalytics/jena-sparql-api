package org.aksw.jena_sparql_api.views;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.stmt.SparqlStmtIterator;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Test;

import com.google.common.collect.Streams;
import com.google.common.io.CharStreams;

public class TestPropertyBasedRewrite {
	public static Query rewrite(String queryStr, ValueSet<Node> valueSet) {
		return ElementTransformPropertyBasedRewrite.transform(QueryFactory.create(queryStr), valueSet, true);
	}
	
	
	@Test
	public void testBasicPropertyBasedRewrite() throws Exception {
		ValueSet<Node> valueSet = ValueSet.create(false, RDF.type.asNode(), RDFS.label.asNode());
		
		
//		System.out.println(rewrite("SELECT ?p { ?s ?p ?o VALUES (?p) { (<http://foo>) } }", valueSet));
		
//		System.out.println(rewrite("SELECT ?p { ?s ?p ?o }", valueSet));
//		System.out.println(rewrite("SELECT ?p { ?s a ?o }", valueSet));
//		System.out.println(rewrite("SELECT * { ?s a ?t ; <http://foo> ?o ; <http://foo> ?p }", valueSet));
//		System.out.println(rewrite("SELECT ?p { ?s <http://foo> ?o ; <http://foo> ?p }", valueSet));
//		System.out.println(rewrite("SELECT ?p { ?s ?p1 ?o1 ; ?p2 ?o2 }", valueSet));
//		System.out.println(rewrite("SELECT ?p { ?s a ?o ; ?x ?y }", valueSet));
//		System.out.println(rewrite("SELECT ?p { ?s a ?o . FILTER(EXISTS { ?x ?y ?z }) }", valueSet));
//
//		System.out.println(rewrite("SELECT ?p { ?s ?p ?o . FILTER(?p IN (<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>)) }", valueSet));
//		System.out.println(rewrite("SELECT ?p { ?s ?p ?o . FILTER(regex(str(?p), '.*type$')) }", valueSet));
//
//		
//		System.out.println(rewrite("SELECT * {\n" + 
//				"  {\n" + 
//				"    ?s ?p ?o .\n" + 
//				"    FILTER(?p IN (<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>))\n" + 
//				"  }\n" + 
//				"  UNION {\n" + 
//				"    ?x ?y ?z .\n" + 
//				"    FILTER(?y IN (<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>))\n" + 
//				"  }\n" + 
//				"}", valueSet));

		System.out.println(rewrite("SELECT ?p { GRAPH ?g { ?s ?p ?o } }", valueSet));

		//System.out.println(rewrite("SELECT ?p { ?s ?p ?o }", valueSet));
	}

	
	//@Test
	public void testPropertyBasedRewriteFromCmem() throws Exception {
		ValueSet<Node> valueSet = ValueSet.create(true, RDF.type.asNode(), RDFS.label.asNode());

		
		InputStream in = new FileInputStream(new File("/home/raven/Projects/Eclipse/eccenca/cmem/prettified-queries.sparql"));
		String str = CharStreams.toString(new InputStreamReader(in, StandardCharsets.UTF_8));

		PrefixMapping pm = new PrefixMappingImpl();
		//pm.setNsPrefixes(PrefixMapping.Extended);

		Function<String, SparqlStmt> sparqlStmtParser =
			SparqlStmtParserImpl.create(Syntax.syntaxARQ, pm, true);

		List<SparqlStmt> stmts = Streams.stream(new SparqlStmtIterator(sparqlStmtParser, str)).collect(Collectors.toList());

		for(SparqlStmt stmt : stmts) {
			String orig = Objects.toString(stmt.getAsQueryStmt().getQuery());
			System.out.println("Original: " + orig);
			Query q = rewrite(orig, valueSet);
			System.out.println("Rewritten: " + q);
		}
		
		System.out.println("Stmts: " + stmts.size());
	}
}
