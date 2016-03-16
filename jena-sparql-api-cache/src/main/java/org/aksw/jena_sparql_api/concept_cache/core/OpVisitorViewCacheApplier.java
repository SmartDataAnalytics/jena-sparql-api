package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.Collection;
import java.util.List;

import org.aksw.jena_sparql_api.concept_cache.dirty.CacheResult;
import org.aksw.jena_sparql_api.concept_cache.dirty.ConceptMap;
import org.aksw.jena_sparql_api.concept_cache.domain.ProjectedQuadFilterPattern;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPattern;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.concept_cache.op.OpUtils;
import org.aksw.jena_sparql_api.util.frontier.Frontier;
import org.aksw.jena_sparql_api.util.frontier.FrontierImpl;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpNull;
import org.apache.jena.sparql.algebra.op.OpTable;


public class OpVisitorViewCacheApplier
    //extends OpVisitorByType
{
    public static void apply(Op _op, ConceptMap conceptMap) {

        Frontier<Op> frontier = new FrontierImpl<>();
        frontier.add(_op);


        while(!frontier.isEmpty()) {
            Op op = frontier.next();

            ProjectedQuadFilterPattern pqfp = SparqlCacheUtils.transform(op);

            if(pqfp == null) {
                List<Op> subOps = OpUtils.getSubOps(op);
                subOps.forEach(frontier::add);
            } else {
                QuadFilterPattern qfp = pqfp.getQuadFilterPattern();

                CacheResult cacheResult = conceptMap.lookup(qfp);

                // Whether the query was exactly the same entry in the cache, so
                // that there is no need cache again
                boolean queryEqualedCache = false;

                if(cacheResult != null) {
                    //CacheHit cacheHit = cacheHits.iterator().next();
                    //CacheResult cacheResult = cacheHits;
                    QuadFilterPatternCanonical qfpc = cacheResult.getReplacementPattern();

                    queryEqualedCache = qfpc.isEmpty();
                    //QuadFilterPatternCanonical remainder = qfpc.diff(queryQfpc);
                    //queryEqualedCache = remainder.isEmpty();

                    Op o = qfpc.toOp();

                    Collection<Table> tables = cacheResult.getTables();
                    Op opTable = null;
                    for(Table table : tables) {
                        Op tmp = OpTable.create(table);

                        if(opTable == null) {
                            opTable = tmp;
                        } else {
                            opTable = OpJoin.create(opTable, tmp);
                        }
                    }


                    //System.out.println("Table size: " + table.size());

                    if(o instanceof OpNull) {
                        o = opTable;
                    } else {
                        o = OpJoin.create(opTable, o);
                    }


                }

            }

        }
        //OpUtils.getSubOps(op)
    }
}
