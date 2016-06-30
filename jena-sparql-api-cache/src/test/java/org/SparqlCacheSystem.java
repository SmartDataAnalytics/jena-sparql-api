package org;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.isomorphism.ProblemContainerNeighbourhoodAware;
import org.aksw.isomorphism.ProblemNeighborhoodAware;
import org.aksw.jena_sparql_api.concept_cache.collection.FeatureMap;
import org.aksw.jena_sparql_api.concept_cache.combinatorics.ProblemVarMappingExpr;
import org.aksw.jena_sparql_api.concept_cache.combinatorics.ProblemVarMappingQuad;
import org.aksw.jena_sparql_api.concept_cache.core.SparqlCacheUtils;
import org.aksw.jena_sparql_api.utils.MapUtils;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Multimap;

public class SparqlCacheSystem {

    protected IndexSystem<Entry<Op, QueryIndex>, Op> indexSystem;
    protected Function<Op, QueryIndex> queryIndexer;
    //protected Map<Op, D> opToCacheData;

    public SparqlCacheSystem() {
        indexSystem = IndexSystemImpl.create();
        queryIndexer = new QueryIndexerImpl();
    }

    public void registerCache(String name, Op cacheOp) { //, D cacheData) {
        QueryIndex queryIndex = queryIndexer.apply(cacheOp);

        // This is the op level indexing of cache
        indexSystem.add(new SimpleEntry<>(cacheOp, queryIndex));
        //opToCacheData.put(cacheOp, cacheData);
    }


    public void rewriteQuery(Op queryOp) {
        QueryIndex queryIndex = queryIndexer.apply(queryOp);

        Collection<Entry<Op, QueryIndex>> candidates = indexSystem.lookup(queryOp);

        for(Entry<Op, QueryIndex> e : candidates) {
            QueryIndex cacheIndex = e.getValue();
            FeatureMap<Expr, QuadPatternIndex> cacheQpi = cacheIndex.getQuadPatternIndex();

            for(Entry<Set<Expr>, Collection<QuadPatternIndex>> f : queryIndex.getQuadPatternIndex().entrySet()) {
                Set<Expr> queryFeatureSet = f.getKey();
                Collection<QuadPatternIndex> queryQps = f.getValue();

                //Collection<Entry<Set<Expr>, QuadPatternIndex>> cacheQpiCandidates = cacheQpi.getIfSupersetOf(queryFeatureSet);
                Collection<Entry<Set<Expr>, QuadPatternIndex>> cacheQpiCandidates = cacheQpi.getIfSubsetOf(queryFeatureSet);

                for(QuadPatternIndex queryQp : queryQps) {
                    for(Entry<Set<Expr>, QuadPatternIndex> g : cacheQpiCandidates) {
                        QuadPatternIndex cacheQp = g.getValue();

                        System.out.println("CacheQP: " + cacheQp);
                        System.out.println("QueryQP: " + queryQp);

                        generateVarMappings(cacheQp, queryQp)
                            .forEach(x -> {
                                System.out.println("solution: " + x);
                                //cacheQp.getGroupedConjunction()

                            });
                        System.out.println("-----");

                    }
                }



            }


        }
    }


    public static Stream<Map<Var, Var>> generateVarMappings(QuadPatternIndex cache, QuadPatternIndex query) {
        Multimap<Expr, Expr> cacheMap = cache.getGroupedConjunction();
        Multimap<Expr, Expr> queryMap = query.getGroupedConjunction();

        Map<Expr, Entry<Set<Expr>, Set<Expr>>> group = MapUtils.groupByKey(cacheMap.asMap(), queryMap.asMap());

        Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> problems = new ArrayList<>();

        group.values().stream()
            .map(x -> {
                Set<Expr> cacheExprs = x.getKey();
                Set<Expr> queryExprs = x.getValue();
                ProblemNeighborhoodAware<Map<Var, Var>, Var> p = new ProblemVarMappingExpr(cacheExprs, queryExprs, Collections.emptyMap());

                //System.out.println("cacheExprs: " + cacheExprs);
                //System.out.println("queryExprs: " + queryExprs);

                //Stream<Map<Var, Var>> r = p.generateSolutions();

                return p;
            })
            .forEach(problems::add);

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
}
