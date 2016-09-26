package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.Collection;

import org.aksw.jena_sparql_api.concept_cache.domain.ProjectedQuadFilterPattern;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.utils.VarGeneratorImpl2;
import org.aksw.jena_sparql_api.views.index.LookupResult;
import org.aksw.jena_sparql_api.views.index.OpViewMatcher;
import org.aksw.jena_sparql_api.views.index.SparqlCacheSystem;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.optimize.Rewrite;

/**
 * Rewrite an algebra expression under view matching
 *
 * @author raven
 *
 */
public class OpRewriteViewMatcher
	implements Rewrite
{
	protected OpViewMatcher viewMatcherTreeBased;
	protected SparqlCacheSystem viewMatcherQuadPatternBased;


	//@Override
	public Node add(Op op) {
		ProjectedQuadFilterPattern conjunctiveQuery = SparqlCacheUtils.transform(op);
		if(conjunctiveQuery != null) {
			QuadFilterPatternCanonical qfpc = SparqlCacheUtils.canonicalize2(conjunctiveQuery.getQuadFilterPattern(), VarGeneratorImpl2.create());


			//qfpcIndex.index(qfpc, null);


			//System.out.println("GOT ECQ");
			//addConjunctiveQuery(item, conjunctiveQuery);

			return null;
		} else {
			viewMatcherTreeBased.add(op);
		}

		return null;

	}


	@Override
	public Op rewrite(Op op) {



		// Attempt to replace complete subtrees


		// Find further substitution candidates for all (canonical) quad pattern leafs


		// TODO Auto-generated method stub
		return null;
	}




//
//	@Override
//	public boolean acceptsAdd(Op op) {
//		// TODO Auto-generated method stub
//		return false;
//	}



}
