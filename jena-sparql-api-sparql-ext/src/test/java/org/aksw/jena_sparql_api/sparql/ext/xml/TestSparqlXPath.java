package org.aksw.jena_sparql_api.sparql.ext.xml;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.Resources;


public class TestSparqlXPath {
	@Test
	public void testSparqlXPath() throws IOException, URISyntaxException {
		URL url = Resources.getResource("sparql-ext-test-xml-01.sparql");
		String text = Resources.toString(url, StandardCharsets.UTF_8);
	
		Query query = QueryFactory.create(text);
		Op op = Algebra.compile(query);
//		System.out.println(op);
		
		List<String> actual = new ArrayList<>();
		try(RDFConnection conn = RDFConnectionFactory.connect(DatasetFactory.create())) {
			conn.querySelect(query, b -> actual.add(b.get("str").toString()));
//			try(QueryExecution qe = conn.query(query)) {
//				System.out.println(ResultSetFormatter.asText(qe.execSelect()));
//			}
			//Model result = conn.queryConstruct(text);
			//RDFDataMgr.write();
		}
		
		Assert.assertEquals(Arrays.asList("1", "2"), actual);
	}
}
