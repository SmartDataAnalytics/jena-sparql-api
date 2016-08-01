package org.aksw.jena_sparql_api.sparql.algebra.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.combinatorics.solvers.ProblemContainerNeighbourhoodAware;
import org.aksw.combinatorics.solvers.ProblemNeighborhoodAware;
import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.aksw.jena_sparql_api.concept_cache.collection.FeatureMap;
import org.aksw.jena_sparql_api.concept_cache.combinatorics.ProblemVarMappingExpr;
import org.aksw.jena_sparql_api.concept_cache.combinatorics.ProblemVarMappingQuad;
import org.aksw.jena_sparql_api.concept_cache.core.SparqlCacheUtils;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.utils.MapUtils;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;

import com.google.common.collect.Multimap;

public class VarMapper {

    public static Stream<Map<Var, Var>> createVarMapCandidates(QuadFilterPatternCanonical cachePattern, QuadFilterPatternCanonical queryPattern) {

        FeatureMap<Expr, Multimap<Expr, Expr>> cacheIndex = SparqlCacheUtils.indexDnf(cachePattern.getFilterDnf());
        FeatureMap<Expr, Multimap<Expr, Expr>> queryIndex = SparqlCacheUtils.indexDnf(queryPattern.getFilterDnf());

        Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> problems = new ArrayList<>();
        for(Entry<Set<Expr>, Collection<Multimap<Expr, Expr>>> entry : queryIndex.entrySet()) {
            Set<Expr> querySig = entry.getKey();
            Collection<Multimap<Expr, Expr>> queryMaps = entry.getValue();

            System.out.println("CAND LOOKUP with " + querySig);
            Collection<Entry<Set<Expr>, Multimap<Expr, Expr>>> cands = cacheIndex.getIfSubsetOf(querySig);

            for(Entry<Set<Expr>, Multimap<Expr, Expr>> e : cands) {
                Multimap<Expr, Expr> cacheMap = e.getValue();
                System.out.println("  CACHE MAP: " + cacheMap);
                for(Multimap<Expr, Expr> queryMap : queryMaps) {
                    Map<Expr, Entry<Set<Expr>, Set<Expr>>> group = MapUtils.groupByKey(cacheMap.asMap(), queryMap.asMap());

                    Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> localProblems = group.values().stream()
                        .map(x -> {
                            Set<Expr> cacheExprs = x.getKey();
                            Set<Expr> queryExprs = x.getValue();
                            ProblemNeighborhoodAware<Map<Var, Var>, Var> p = new ProblemVarMappingExpr(cacheExprs, queryExprs, Collections.emptyMap());

                            System.out.println("Registered problem instance " + p + " with an estimated cost of " + p.getEstimatedCost());
                            System.out.println("  Enumerating its solutions yields " + p.generateSolutions().count() + " items: " + p.generateSolutions().collect(Collectors.toList()));
                            //System.out.println("cacheExprs: " + cacheExprs);
                            //System.out.println("queryExprs: " + queryExprs);

                            //Stream<Map<Var, Var>> r = p.generateSolutions();

                            return p;
                        })
                        .collect(Collectors.toList());

                    problems.addAll(localProblems);
                    //problems.stream().forEach(p -> System.out.println("COMPLEX: " + p.getEstimatedCost()));


                    //problemStream.forEach(y -> System.out.println("GOT SOLUTION: " + y));



                    //System.out.println("    QUERY MAP: " + queryMap);
                }
            }

            //cands.forEach(x -> System.out.println("CAND: " + x.getValue()));
        }

        // Index the quads by the cnf (dnf)?
        
        IBiSetMultimap<Quad, Set<Set<Expr>>> cacheQuadIndex = SparqlCacheUtils.createMapQuadsToFilters(cachePattern);
        IBiSetMultimap<Quad, Set<Set<Expr>>> queryQuadIndex = SparqlCacheUtils.createMapQuadsToFilters(queryPattern);
        
        Map<Set<Set<Expr>>, Entry<Set<Quad>, Set<Quad>>> quadGroups = MapUtils.groupByKey(cacheQuadIndex.getInverse(), queryQuadIndex.getInverse());
        
        for(Entry<Set<Quad>, Set<Quad>> quadGroup : quadGroups.values()) {
        
            ProblemVarMappingQuad quadProblem = new ProblemVarMappingQuad(quadGroup.getKey(), quadGroup.getValue(), Collections.emptyMap());

            System.out.println("Registered quad problem instance " + quadProblem + " with an estimated cost of " + quadProblem.getEstimatedCost());
            //System.out.println("  Enumerating its solutions yields " + quadProblem.generateSolutions().count());
            //System.out.println("Registered quad problem instance " + quadProblem + " with " + quadProblem.generateSolutions().count() + " solutions ");
            
            
            problems.add(quadProblem);
        }
        
        
        
        Stream<Map<Var, Var>> result = ProblemContainerNeighbourhoodAware.solve(
                problems,
                Collections.emptyMap(),
                Map::keySet,
                MapUtils::mergeIfCompatible,
                Objects::isNull);

        return result;
    }
}
