package org.aksw.jena_sparql_api.conjure.fluent;

import java.util.function.Function;

import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.apache.jena.query.Syntax;

/**
 * Context for fluent; contains the SPARQL statement parser for early validation
 * 
 * @author raven
 *
 */
public class ConjureContext {
	protected Function<String, SparqlStmt> sparqlStmtParser;

	public ConjureContext() {
		this(SparqlStmtParserImpl.create(Syntax.syntaxARQ, DefaultPrefixes.prefixes, false));
	}

	public ConjureContext(Function<String, SparqlStmt> sparqlStmtParser) {
		super();
		this.sparqlStmtParser = sparqlStmtParser;
	}

	public Function<String, SparqlStmt> getSparqlStmtParser() {
		return sparqlStmtParser;
	}
}
