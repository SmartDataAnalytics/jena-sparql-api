package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.concept_cache.dirty.CacheResult;
import org.aksw.jena_sparql_api.concept_cache.dirty.ConceptMap;
import org.aksw.jena_sparql_api.concept_cache.domain.ProjectedQuadFilterPattern;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPattern;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.concept_cache.op.OpUtils;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpNull;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.core.Var;

public class OpVisitorViewCacheApplier
// extends OpVisitorByType
{
    public static Query apply(Query query, ConceptMap conceptMap) {
        // Query yay = OpAsQuery.asQuery(op);
        Op originalOp = Algebra.compile(query);

        Op rewrittenOp = apply(originalOp, conceptMap);
        boolean isPatternFree = OpUtils.isPatternFree(rewrittenOp);


        Query result = OpAsQuery.asQuery(rewrittenOp);

        result.setQueryResultStar(false);
        result.getProjectVars().clear();
        for (Var x : query.getProjectVars()) {
            result.getProject().add(x);
        }

        // TODO We need to reset the projection...
        return result;
    }

    public static Op apply(Op parentOp, ConceptMap conceptMap) {

        Op result;

        ProjectedQuadFilterPattern pqfp = SparqlCacheUtils.transform(parentOp);

        if (pqfp == null) {
            // Recursively descend to the children
            List<Op> subOps = OpUtils.getSubOps(parentOp);
            List<Op> newSubOps = subOps.stream()
                    .map(child -> apply(child, conceptMap))
                    .collect(Collectors.toList());

            result = OpUtils.copy(parentOp, newSubOps);

        } else {
            QuadFilterPattern qfp = pqfp.getQuadFilterPattern();

            CacheResult cacheResult = conceptMap.lookup(qfp);

            // Whether the query was exactly the same entry in the cache, so
            // that there is no need cache again
            boolean queryEqualedCache = false;

            if (cacheResult != null) {
                // CacheHit cacheHit = cacheHits.iterator().next();
                // CacheResult cacheResult = cacheHits;
                QuadFilterPatternCanonical qfpc = cacheResult
                        .getReplacementPattern();

                queryEqualedCache = qfpc.isEmpty();

                System.out.println("Is same: " + queryEqualedCache);
                // QuadFilterPatternCanonical remainder = qfpc.diff(queryQfpc);
                // queryEqualedCache = remainder.isEmpty();

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
            } else {
                // There was no cache hit, just return the op
                result = parentOp;
            }

        }
        return result;
    }
}
