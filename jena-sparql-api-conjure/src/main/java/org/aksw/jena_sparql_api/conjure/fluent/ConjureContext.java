package org.aksw.jena_sparql_api.conjure.fluent;

import java.util.function.Function;

import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParser;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * Context for fluent; contains the SPARQL statement parser for early validation
 * 
 * @author raven
 *
 */
public class ConjureContext {
	/**
	 * The model into which to create the ops and other resources 
	 */
	protected Model model;
	protected Function<String, SparqlStmt> sparqlStmtParser;

	public ConjureContext() {
		this(
			ModelFactory.createDefaultModel(),
			SparqlStmtParser.wrapWithOptimizePrefixes(
					SparqlStmtParserImpl.create(Syntax.syntaxARQ, DefaultPrefixes.prefixes, false)));
	}

	public ConjureContext(Model model, Function<String, SparqlStmt> sparqlStmtParser) {
		super();
		this.model = model;
		this.sparqlStmtParser = sparqlStmtParser;
	}
	
	public Model getModel() {
		return model;
	}

	public Function<String, SparqlStmt> getSparqlStmtParser() {
		return sparqlStmtParser;
	}
}
