package org.aksw.jena_sparql_api.sparql.ext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.aksw.jena_sparql_api.sparql.ext.util.JenaExtensionUtil;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.junit.Test;

public class TestUrlText {
	//@Test
	public void testUrlTest() throws IOException {
		PrefixMapping pm = new PrefixMappingImpl();
		pm.setNsPrefixes(PrefixMapping.Extended);
		JenaExtensionUtil.addPrefixes(pm);

		
		String str = IOUtils.toString(TestUrlText.class.getClassLoader().getResourceAsStream("url-text.sparql"), StandardCharsets.UTF_8);

		Query query = new Query();
		query.setPrefixMapping(pm);
		Dataset dataset = DatasetFactory.create();
		QueryFactory.parse(query, str, "http://foo/", Syntax.syntaxARQ);
		Op op = Algebra.compile(query);
		System.out.println("Algebra: " + op);
		
		try(QueryExecution qe = QueryExecutionFactory.create(query, dataset)) {
			Model m = qe.execConstruct();
			RDFDataMgr.write(System.out, m, RDFFormat.TURTLE_PRETTY);
//			ResultSet rs = qe.execSelect();
//			System.out.println(ResultSetFormatter.asText(rs));
		}
	}
}
