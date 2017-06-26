package org.aksw.jena_sparql_api.views.index;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.aksw.combinatorics.solvers.ProblemContainerNeighbourhoodAware;
import org.aksw.combinatorics.solvers.ProblemNeighborhoodAware;
import org.aksw.commons.collections.FeatureMap;
import org.aksw.commons.collections.trees.Tree;
import org.aksw.jena_sparql_api.concept_cache.combinatorics.ProblemVarMappingExpr;
import org.aksw.jena_sparql_api.concept_cache.combinatorics.ProblemVarMappingQuad;
import org.aksw.jena_sparql_api.utils.MapUtils;
import org.aksw.jena_sparql_api.view_matcher.OpVarMap;
import org.aksw.jena_sparql_api.view_matcher.SparqlViewMatcherUtils;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class SparqlViewMatcherSystemImpl
	implements SparqlViewMatcherSystem
{
	private static final Logger logger = LoggerFactory.getLogger(SparqlViewMatcherSystemImpl.class);


    protected IndexSystem<Entry<Op, OpIndex>, Op> indexSystem;
    protected Function<Op, OpIndex> queryIndexer;




    //protected Map<Op, D> opToCacheData;

    public SparqlViewMatcherSystemImpl() {
        this.indexSystem = IndexSystemImpl.create();
        this.queryIndexer = new OpIndexerImpl();
    }

    public void registerView(String name, Op cacheOp) { //, D cacheData) {
        OpIndex queryIndex = queryIndexer.apply(cacheOp);

        // This is the op level indexing of cache
        indexSystem.add(new SimpleEntry<>(cacheOp, queryIndex));
        //opToCacheData.put(cacheOp, cacheData);
    }


    @Override
    public Op rewriteQuery(Op queryOp) {
        OpIndex queryIndex = queryIndexer.apply(queryOp);

        // Create the initial set of cache candidates based on the query's algebra
        Collection<Entry<Op, OpIndex>> candidates = indexSystem.lookup(queryOp);

        for(Entry<Op, OpIndex> e : candidates) {
            OpIndex cacheIndex = e.getValue();


            Multimap<Op, Op> candidateLeafMapping = getCandidateLeafMapping(cacheIndex, queryIndex);

            System.out.println("Leaf Mapping: " + candidateLeafMapping);
        }

        return null;
    }


    public static Multimap<Op, Op> getCandidateLeafMapping(OpIndex cacheIndex, OpIndex queryIndex) {

        Multimap<Op, Op> result = HashMultimap.create();

        //QueryIndex cacheIndex = e.getValue();
        FeatureMap<Expr, QuadPatternIndex> cacheQpi = cacheIndex.getQuadPatternIndex();

        for(Entry<Set<Expr>, Collection<QuadPatternIndex>> f : queryIndex.getQuadPatternIndex().entrySet()) {
            Set<Expr> queryFeatureSet = f.getKey();
            Collection<QuadPatternIndex> queryQps = f.getValue();

            //Collection<Entry<Set<Expr>, QuadPatternIndex>> cacheQpiCandidates = cacheQpi.getIfSupersetOf(queryFeatureSet);
            Collection<Entry<Set<Expr>, QuadPatternIndex>> cacheQpiCandidates = cacheQpi.getIfSubsetOf(queryFeatureSet);

            for(QuadPatternIndex queryQp : queryQps) {
                Op queryLeaf = queryQp.getOpRef().getNode();



                for(Entry<Set<Expr>, QuadPatternIndex> g : cacheQpiCandidates) {
                    QuadPatternIndex cacheQp = g.getValue();

                    Op cacheLeaf = cacheQp.getOpRef().getNode();

                    result.put(cacheLeaf, queryLeaf);
//
//                        System.out.println("CacheQP: " + cacheQp);
//                        System.out.println("QueryQP: " + queryQp);
//
//                        generateVarMappings(cacheQp, queryQp)
//                            .forEach(x -> {
//                                System.out.println("solution: " + x);
//                                //cacheQp.getGroupedConjunction()
//
//                            });
//                        System.out.println("-----");

                }
            }
        }


        return result;
    }

    public static Stream<ProblemNeighborhoodAware<Map<Var, Var>, Var>> createProblems(Multimap<Expr, Expr> sigToCache, Multimap<Expr, Expr> sigToQuery) {
        Map<Expr, Entry<Set<Expr>, Set<Expr>>> group = MapUtils.groupByKey(sigToCache.asMap(), sigToQuery.asMap());

        Stream<ProblemNeighborhoodAware<Map<Var, Var>, Var>> result = group.values().stream()
            .map(x -> {
                Set<Expr> cacheExprs = x.getKey();
                Set<Expr> queryExprs = x.getValue();
                ProblemNeighborhoodAware<Map<Var, Var>, Var> p = new ProblemVarMappingExpr(cacheExprs, queryExprs, Collections.emptyMap());
                return p;
            });

        return result;
    }

    public static Stream<Map<Var, Var>> generateVarMappings(QuadPatternIndex cache, QuadPatternIndex query) {
        Multimap<Expr, Expr> cacheMap = cache.getGroupedConjunction();
        Multimap<Expr, Expr> queryMap = query.getGroupedConjunction();

        Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> problems = new ArrayList<>();

        createProblems(cacheMap, queryMap).forEach(problems::add);


//        group.values().stream()
//            .map(x -> {
//                Set<Expr> cacheExprs = x.getKey();
//                Set<Expr> queryExprs = x.getValue();
//                ProblemNeighborhoodAware<Map<Var, Var>, Var> p = new ProblemVarMappingExpr(cacheExprs, queryExprs, Collections.emptyMap());
//
//                //System.out.println("cacheExprs: " + cacheExprs);
//                //System.out.println("queryExprs: " + queryExprs);
//
//                //Stream<Map<Var, Var>> r = p.generateSolutions();
//
//                return p;
//            })
//            .forEach(problems::add);

        ProblemVarMappingQuad quadProblem = new ProblemVarMappingQuad(cache.getQfpc().getQuads(), query.getQfpc().getQuads(), Collections.emptyMap());
        problems.add(quadProblem);

//        for(int i = 0; i < 1000; ++i) {
//            Stopwatch sw = Stopwatch.createStarted();

        Stream<Map<Var, Var>> result = ProblemContainerNeighbourhoodAware.solve(
                problems,
                Collections.emptyMap(),
                Map::keySet,
                MapUtils::mergeIfCompatible,
                Objects::isNull);
//        }
        return result;
    }



    @Deprecated
    // TODO: This method is similar to SparqlQueryContainmentUtils.tryMatch
    public static Stream<OpVarMap> match(Query a, Query b) {
    	Function<Op, OpIndex> opIndexer = new OpIndexerImpl();


    	OpIndex viewIndex = opIndexer.apply(SparqlViewMatcherOpImpl.queryToNormalizedOp(a));
    	OpIndex queryIndex = opIndexer.apply(SparqlViewMatcherOpImpl.queryToNormalizedOp(b));


        Multimap<Op, Op> candOpMapping = SparqlViewMatcherSystemImpl.getCandidateLeafMapping(viewIndex, queryIndex);

        if(logger.isDebugEnabled()) {
        	for(Entry<Op, Collection<Op>> e : candOpMapping.asMap().entrySet()) {
        		logger.debug("Candidate leaf mapping: " + e.getKey());
        		for(Op f : e.getValue()) {
        			logger.debug("  Target: " + f);
        		}
        	}
        }


        Tree<Op> cacheTree = viewIndex.getTree();
        Tree<Op> queryTree = queryIndex.getTree();

        // TODO: Require a complete match of the tree - i.e. cache and query trees must have same number of nodes / same depth / some other criteria that can be checked quickly
        // In fact, we could use these features as an additional index
        Stream<OpVarMap> result = SparqlViewMatcherUtils.generateTreeVarMapping(candOpMapping, cacheTree, queryTree);

        return result;
    }


    /**
     *
     * @param cacheParentToNodeMaps: Mapping from a cache parent, to the candidate op-mappings from cache op to query op. Example: { 5: { A: {1}, B: {1} } }
     *
     */
    public static void matchOpTrees(Map<Op, Multimap<Op, Op>> cacheParentToNodeMaps, Tree<Op> cacheTree, Tree<Op> queryTree) {

    }
}





//public static <T> Tree<T> removeUnaryNodes(Tree<T> tree) {
//
//  Predicate<T> isMultiary = (node) -> tree.getChildren(node).size() > 1;
//
//  //Map<T, T> childToParent = new HashMap<>();
//  ListMultimap<T, T> parentToChildren = ArrayListMultimap.create();
//
//  // for every leaf get the first non-unary parent
//  Collection<T> parents = TreeUtils.getLeafs(tree);
//  Collection<T> children = null;
//  while(!parents.isEmpty()) {
//      children = parents;
//
//      parents = new LinkedHashSet<T>();
//      for(T child : children) {
//          T parent = TreeUtils.findAncestor(tree, child, isMultiary);
//          if(parent != null) {
//              parents.add(parent);
//              parentToChildren.put(parent, child);
//          }
//      }
//  }
//
//  // There can be at most 1 root
//  T root = children.iterator().next(); //parents.isEmpty() ? null : parents.iterator().next();
//
//
//  Tree<T> result = root == null
//          ? null
//          : TreeImpl.create(root, (node) -> parentToChildren.get(node));
//
//  return result;
//}
