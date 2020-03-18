package org.aksw.jena_sparql_api.stmt;

import java.util.Set;
import java.util.function.Function;

import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.update.UpdateRequest;
import org.junit.Assert;
import org.junit.Test;

public class TestSparqlStmtUtils {
	
	@Test
	public void testSparqlSelectShortFormSimple() {
		Function<String, Query> parser = SparqlQueryParserWrapperSelectShortForm.wrap(SparqlQueryParserImpl.create());

		Query expected = QueryFactory.create("SELECT ?s { ?s ?p ?o }");
		Query actual = parser.apply("?s { ?s ?p ?o }");
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testSparqlSelectShortFormWithPrefixes() {
		Function<String, Query> parser = SparqlQueryParserWrapperSelectShortForm.wrap(SparqlQueryParserImpl.create());

		Query expected = QueryFactory.create("PREFIX eg: <http://www.example.org/> SELECT ?s { ?s eg:foo ?o }");
		Query actual = parser.apply("PREFIX eg: <http://www.example.org/> ?s { ?s eg:foo ?o }");
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testUsedPrefixes1() {
		PrefixMapping pm = RDFDataMgr.loadModel("rdf-prefixes/wikidata.jsonld");
		String queryStr = "SELECT ?s ?desc WHERE {\n" + 
				"  ?s wdt:P279 wd:Q7725634 .\n" + 
				"  OPTIONAL {\n" + 
				"      ?s rdfs:label ?desc \n" + 
				"      FILTER (LANG(?desc) = \"en\").\n" + 
				"  }\n" + 
				"}";
		
		SparqlStmt stmt = SparqlStmtParserImpl.create(pm).apply(queryStr);
		SparqlStmtUtils.optimizePrefixes(stmt);
		Query query = stmt.getQuery();
		Set<String> actual = query.getPrefixMapping().getNsPrefixMap().keySet();
		Assert.assertEquals(Sets.newHashSet("rdfs", "wd", "wdt"), actual);		
	}

	@Test
	public void testUsedPrefixes2() {
		PrefixMapping pm = RDFDataMgr.loadModel("rdf-prefixes/wikidata.jsonld");
		String queryStr = "INSERT {" + 
				"  ?s wdt:P279 wd:Q7725634 .\n" +
				"}\n" +
				"  WHERE {\n" + 
				"      ?s rdfs:label ?desc \n" + 
				"      FILTER (LANG(?desc) = \"en\").\n" + 
				"  }\n";
		
		SparqlStmt stmt = SparqlStmtParserImpl.create(pm).apply(queryStr);
		SparqlStmtUtils.optimizePrefixes(stmt);
		UpdateRequest updateRequest = stmt.getUpdateRequest();
		// System.out.println(updateRequest);
		Set<String> actual = updateRequest.getPrefixMapping().getNsPrefixMap().keySet();
		Assert.assertEquals(Sets.newHashSet("rdfs", "wd", "wdt"), actual);		
	}

}
