package org.aksw.jena_sparql_api.concept_cache.core;

import org.aksw.jena_sparql_api.algebra.utils.ProjectedOp;

public interface RewriterSparqlViewMatcher {
	RewriteResult2 rewrite(ProjectedOp op);
}
