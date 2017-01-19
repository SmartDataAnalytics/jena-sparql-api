package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.Map;

import org.aksw.jena_sparql_api.views.index.SparqlViewMatcherOpImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.optimize.Rewrite;

/**
 * Wrapper around a view matcher that handles with top level
 * - projection and
 * - filters
 * over a core query expression.
 *
 * @author raven
 *
 */
public class OpRewriteViewMatcherProjection
	//implements RewriterSparqlViewMatcher
{
    protected Rewrite opNormalizer;

    protected RewriterSparqlViewMatcher delegate;

    protected Map<Node, Map<Node, VarInfo>> patternIdToStorageIdToVarInfo;
    protected Map<Node, Node> storageIdToPatternId;


	//@Override
	public RewriteResult2 rewrite(ProjectedOp storageOp) {
        //ProjectedOp pop = SparqlCacheUtils.cutProjectionAndNormalize(storageOp, SparqlViewMatcherOpImpl::normalizeOp);

        Op patternOp = storageOp.getResidualOp();

        RewriteResult2 rr = delegate.rewrite(patternOp);
        //rr.get

        return null;
	}

	public void put(Node storageId, ProjectedOp storageOp) {
	}
}
