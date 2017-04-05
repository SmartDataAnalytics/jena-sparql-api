package org.aksw.jena_sparql_api.views.index;

import org.apache.jena.query.Query;

@FunctionalInterface
public interface QueryRewriter {
	Query rewrite(Query query);
}
