package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.aksw.commons.collections.trees.Tree;
import org.aksw.commons.collections.trees.TreeUtils;
import org.aksw.commons.util.strings.StringUtils;
import org.aksw.jena_sparql_api.concept_cache.dirty.SparqlViewCache;
import org.aksw.jena_sparql_api.concept_cache.domain.ProjectedQuadFilterPattern;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.concept_cache.op.OpUtils;
import org.aksw.jena_sparql_api.utils.VarGeneratorImpl2;
import org.aksw.jena_sparql_api.view_matcher.OpVarMap;
import org.aksw.jena_sparql_api.views.index.LookupResult;
import org.aksw.jena_sparql_api.views.index.OpViewMatcher;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.optimize.Rewrite;
import org.apache.jena.sparql.core.Var;

/**
 * Rewrite an algebra expression under view matching
 *
 * @author raven
 *
 */
public class OpRewriteViewMatcher
	implements Rewrite
{
	protected Rewrite opNormalizer;
	protected OpViewMatcher viewMatcherTreeBased;
	protected SparqlViewCache<Node> viewMatcherQuadPatternBased;


	//@Override
	public Node add(Op op) {
		Node result;

		ProjectedQuadFilterPattern conjunctiveQuery = SparqlCacheUtils.transform(op);
		if(conjunctiveQuery != null) {
			QuadFilterPatternCanonical qfpc = SparqlCacheUtils.canonicalize2(conjunctiveQuery.getQuadFilterPattern(), VarGeneratorImpl2.create());


			//result =
			viewMatcherQuadPatternBased.put(qfpc, null);


			//System.out.println("GOT ECQ");
			//addConjunctiveQuery(item, conjunctiveQuery);

			return null;
		} else {
			result = viewMatcherTreeBased.add(op);
		}

		return result;

	}


	@Override
	public Op rewrite(Op op) {
    	Op normalizedItem = opNormalizer.rewrite(op);

    	Node id = NodeFactory.createURI("id://" + StringUtils.md5Hash("" + normalizedItem));


    	Op current = op;
    	for(;;) {
			// Attempt to replace complete subtrees
			Collection<LookupResult> lookupResults = viewMatcherTreeBased.lookup(op);

			if(lookupResults == null) {
				break;
			}

			for(LookupResult lr : lookupResults) {
				OpVarMap opVarMap = lr.getOpVarMap();

				Map<Op, Op> opMap = opVarMap.getOpMap();
				Iterable<Map<Var, Var>> varMaps = opVarMap.getVarMaps();

				Node viewId = lr.getEntry().id;
				Op viewRootOp = lr.getEntry().queryIndex.getOp();
				Map<Var, Var> map = Iterables.getFirst(varMaps, null);

				// TODO Properly inject service references into the op node


				// Get the node in the user query which to replace
				Op userSubstOp = opMap.get(viewRootOp);
				Op newNode = new OpService(viewId, new OpQuadBlock(), true);

				current = OpUtils.substitute(current, userSubstOp, newNode);
			}
    	}


		// Find further substitution candidates for all (canonical) quad pattern leafs
    	Tree<Op> tree = OpUtils.createTree(current);
    	List<Op> leafs = TreeUtils.getLeafs(tree);


    	for(Op leafOp : leafs) {
    		ProjectedQuadFilterPattern pqfp = SparqlCacheUtils.transform(op);
    		if(pqfp != null) {

    			QuadFilterPatternCanonical qfpc = SparqlCacheUtils.canonicalize2(pqfp.getQuadFilterPattern(), VarGeneratorImpl2.create());

    			//viewMatcherQuadPatternBased.

    		}


    	}





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
