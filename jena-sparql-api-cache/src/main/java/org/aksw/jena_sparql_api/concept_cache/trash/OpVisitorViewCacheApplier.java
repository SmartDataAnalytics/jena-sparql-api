package org.aksw.jena_sparql_api.concept_cache.trash;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.algebra.utils.OpUtils;
import org.aksw.jena_sparql_api.algebra.utils.ProjectedQuadFilterPattern;
import org.aksw.jena_sparql_api.algebra.utils.QuadFilterPattern;
import org.aksw.jena_sparql_api.algebra.utils.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.algebra.utils.AlgebraUtils;
import org.aksw.jena_sparql_api.concept_cache.core.CacheResult;
import org.aksw.jena_sparql_api.concept_cache.dirty.SparqlViewMatcherQfpc;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpNull;
import org.apache.jena.sparql.algebra.op.OpTable;

public class OpVisitorViewCacheApplier
// extends OpVisitorByType
{
    /**
     *
     * @param query
     * @param conceptMap
     * @return
     */
//    public static RewriteResult apply(Query query, ConceptMap conceptMap) {
//
//
//        Map<Op, CacheResult> opToCover = detectCovers(originalOp, conceptMap);
//
//        // Check if the whole query originates from the cache
//        // if so, prevent re-caching it.
//        CacheResult rootCover = opToCover.get(originalOp);
//        boolean allowCaching = true;
//        if(rootCover != null) {
//            QuadFilterPatternCanonical qfpc = rootCover.getReplacementPattern();
//
//            allowCaching = !qfpc.isEmpty();
//        }
//
//        Op rewrittenOp = applyCovers(originalOp, conceptMap, opToCover);
//        boolean isPatternFree = OpUtils.isPatternFree(rewrittenOp);
//
//
//        Query rewrittenQuery = OpAsQuery.asQuery(rewrittenOp);
//
//        rewrittenQuery.setQueryResultStar(false);
//        rewrittenQuery.getProjectVars().clear();
//        for (Var x : query.getProjectVars()) {
//            rewrittenQuery.getProject().add(x);
//        }
//
//        // TODO We need to reset the projection...
//        // TODO Is above TODO still valid? (Oh the irony)
//
//        RewriteResult result = new RewriteResult(rewrittenQuery, rewrittenOp, allowCaching, isPatternFree);
//
//        return result;
//    }


    /**
     * Second argument is true if the cache completely
     *
     * @param parentOp
     * @param conceptMap
     * @return
     */
    public static Entry<Op, Boolean> applyX(Op parentOp, SparqlViewMatcherQfpc conceptMap) {
        return null;
    }

    /**
     * TODO This should be separated into three phases:
     * 1. Detect covers
     * 2. Chose covers
     * 3. Apply covers
     *
     * Performs a depth first traversal of the op tree and replaces nodes
     * with cache hits.
     *
     *
     * @param parentOp
     * @param conceptMap
     * @return
     */
    public static Map<Op, CacheResult> detectCovers(Op parentOp, SparqlViewMatcherQfpc conceptMap) {
        Map<Op, CacheResult> result = new HashMap<>(); // TODO We might consider using an IdentityHashMap
        detectCovers(parentOp, conceptMap, result);
        return result;
    }


    public static Map<Op, ProjectedQuadFilterPattern> detectPrimitiveCachableOps(Op parentOp) {
        Map<Op, ProjectedQuadFilterPattern> result = new HashMap<>();
        detectPrimitiveCachableOps(parentOp, result);
        return result;
    }

    /**
     * Check the query for ops that can be cached - that is
     * projected quad filter patterns
     *
     * @param parentOp
     * @param result
     */
    public static void detectPrimitiveCachableOps(Op parentOp, Map<Op, ProjectedQuadFilterPattern> result) {
        ProjectedQuadFilterPattern pqfp = AlgebraUtils.transform(parentOp);

        if (pqfp == null) {
            // Recursively descend to the children
            List<Op> subOps = OpUtils.getSubOps(parentOp);
            for(Op subOp : subOps) {
                detectPrimitiveCachableOps(subOp, result);
            }
        } else {
            result.put(parentOp, pqfp);
        }

    }


    public static void detectCovers(Op parentOp, SparqlViewMatcherQfpc conceptMap, Map<Op, CacheResult> result) {

        ProjectedQuadFilterPattern pqfp = AlgebraUtils.transform(parentOp);

        if (pqfp == null) {
            // Recursively descend to the children
            List<Op> subOps = OpUtils.getSubOps(parentOp);
            for(Op subOp : subOps) {
                detectCovers(subOp, conceptMap, result);
            }
        } else {
            QuadFilterPattern qfp = pqfp.getQuadFilterPattern();

            if(true) { throw new RuntimeException("need to fex the lookup"); }
            CacheResult cacheResult = null;//conceptMap.lookup(qfp);

            if(cacheResult != null) {
                result.put(parentOp, cacheResult);
            }
        }
    }


    public static Op applyCovers(Op parentOp, SparqlViewMatcherQfpc conceptMap, Map<Op, CacheResult> opToCover) {

        Op result;

        CacheResult cacheResult = opToCover.get(parentOp);
        if(cacheResult == null) {
            // Recursively descend to the children
            List<Op> subOps = OpUtils.getSubOps(parentOp);
            List<Op> newSubOps = subOps.stream()
                    .map(child -> applyCovers(child, conceptMap, opToCover))
                    .collect(Collectors.toList());

            result = OpUtils.copy(parentOp, newSubOps);
        } else {
            // CacheHit cacheHit = cacheHits.iterator().next();
            // CacheResult cacheResult = cacheHits;
            QuadFilterPatternCanonical qfpc = cacheResult.getReplacementPattern();

            Op o = qfpc.toOp();

            Collection<Table> tables = cacheResult.getTables();
            Op opTable = null;
            for (Table table : tables) {
                Op tmp = OpTable.create(table);

                if (opTable == null) {
                    opTable = tmp;
                } else {
                    opTable = OpJoin.create(opTable, tmp);
                }
            }

            // System.out.println("Table size: " + table.size());

            if (o instanceof OpNull) {
                result = opTable;
            } else {
                result = OpJoin.create(opTable, o);
            }
        }
        return result;
    }
}
