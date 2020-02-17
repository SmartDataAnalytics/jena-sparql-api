package org.aksw.jena_sparql_api.stmt;

import org.apache.jena.query.QueryParseException;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.junit.Test;

public class TestValidateSparqlQueryList {

	/**
	 * Expect a parse error in an erroneous file
	 * 
	 * @throws Exception
	 */
	@Test(expected = QueryParseException.class)
	public void testSparqlFileForParseError() throws Exception {
		PrefixMapping pm = new PrefixMappingImpl();
		pm.setNsPrefixes(PrefixMapping.Extended);

		// TODO Validate the line/column numbers of the syntax error
		
		SparqlStmtIterator it = SparqlStmtUtils.processFile(pm, "syntax-error.sparql");
		while(it.hasNext()) {
			it.next();
			// SparqlStmt stmt = it.next();
			// System.out.println(stmt.getAsQueryStmt().getQuery());			
		}
		
	}
}
