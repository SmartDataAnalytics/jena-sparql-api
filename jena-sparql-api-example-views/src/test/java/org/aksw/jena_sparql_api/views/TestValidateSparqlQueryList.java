package org.aksw.jena_sparql_api.views;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.stmt.SparqlStmtIterator;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.apache.jena.query.Syntax;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.junit.Test;

import com.google.common.collect.Streams;
import com.google.common.io.CharStreams;

public class TestValidateSparqlQueryList {

	@Test
	public void testSparqlTestSuite() throws Exception {
		InputStream in = new FileInputStream(new File("/home/raven/Projects/Eclipse/eccenca/cmem/raw-queries.sparql"));
		String str = CharStreams.toString(new InputStreamReader(in, StandardCharsets.UTF_8));

		PrefixMapping pm = new PrefixMappingImpl();
		//pm.setNsPrefixes(PrefixMapping.Extended);

		Function<String, SparqlStmt> sparqlStmtParser =
			SparqlStmtParserImpl.create(Syntax.syntaxARQ, pm, true);

		List<SparqlStmt> stmts = Streams.stream(new SparqlStmtIterator(sparqlStmtParser, str)).collect(Collectors.toList());

		for(SparqlStmt stmt : stmts) {
			System.out.println(stmt.getAsQueryStmt().getQuery());
		}
		
	}
}
