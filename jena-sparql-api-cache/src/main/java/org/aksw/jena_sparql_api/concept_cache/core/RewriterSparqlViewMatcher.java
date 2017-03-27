package org.aksw.jena_sparql_api.concept_cache.core;

public interface RewriterSparqlViewMatcher {
	RewriteResult2 rewrite(ProjectedOp op);
}
