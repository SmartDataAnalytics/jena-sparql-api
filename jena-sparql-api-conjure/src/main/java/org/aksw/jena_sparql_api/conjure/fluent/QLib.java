package org.aksw.jena_sparql_api.conjure.fluent;

/**
 * Query Library
 * 
 * @author raven
 *
 */
public class QLib {
	public static String tripleCount() {
		return "CONSTRUCT { [] <http://rdfs.org/ns/void#triples> ?c }"
				+ "{ { SELECT (COUNT(*) AS ?c) { ?s ?p ?o } } }";
	}

	public static String everything() {
		return "CONSTRUCT WHERE { ?s ?p ?o }";
	}

	// Just for testing .compose()
	public static ConjureFluent tripleCount(ConjureFluent in) {
		return in.construct(tripleCount());
	}
}
