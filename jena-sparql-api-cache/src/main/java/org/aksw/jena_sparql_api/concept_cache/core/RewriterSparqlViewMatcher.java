package org.aksw.jena_sparql_api.concept_cache.core;

import org.apache.jena.sparql.algebra.Op;

public interface RewriterSparqlViewMatcher {
	RewriteResult2 rewrite(Op op);
}
