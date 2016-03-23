package org.aksw.jena_sparql_api.concept_cache.core;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Op;

class RewriteResult {
    protected Query rewrittenQuery;
    protected Op rewrittenOp;
    protected boolean isCachingAllowed;
    protected boolean isPatternFree;

    public RewriteResult(Query rewrittenQuery, Op rewrittenOp,
            boolean allowCaching, boolean isPatternFree) {
        super();
        this.rewrittenQuery = rewrittenQuery;
        this.rewrittenOp = rewrittenOp;
        this.isCachingAllowed = allowCaching;
        this.isPatternFree = isPatternFree;
    }

    public Query getRewrittenQuery() {
        return rewrittenQuery;
    }

    public Op getRewrittenOp() {
        return rewrittenOp;
    }

    public boolean isCachingAllowed() {
        return isCachingAllowed;
    }

    public boolean isPatternFree() {
        return isPatternFree;
    }
}
