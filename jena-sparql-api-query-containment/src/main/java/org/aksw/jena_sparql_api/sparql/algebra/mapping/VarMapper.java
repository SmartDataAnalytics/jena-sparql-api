package org.aksw.jena_sparql_api.sparql.algebra.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.combinatorics.solvers.GenericProblem;
import org.aksw.combinatorics.solvers.ProblemContainerNeighbourhoodAware;
import org.aksw.combinatorics.solvers.ProblemNeighborhoodAware;
import org.aksw.combinatorics.solvers.ProblemStaticSolutions;
import org.aksw.commons.collections.FeatureMap;
import org.aksw.commons.collections.FeatureMapImpl;
import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.aksw.jena_sparql_api.algebra.utils.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.algebra.utils.AlgebraUtils;
import org.aksw.jena_sparql_api.concept_cache.combinatorics.ProblemVarMappingExpr;
import org.aksw.jena_sparql_api.concept_cache.combinatorics.ProblemVarMappingQuad;
import org.aksw.jena_sparql_api.utils.MapUtils;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;

public class VarMapper {
    private static final Logger logger = LoggerFactory.getLogger(VarMapper.class);


    public static GenericProblem<Map<Var, Var>, ?> deriveProblem(List<Var> cacheVars, List<Var> userVars) {
        List<Expr> aExprs = cacheVars.stream().map(v -> new ExprVar(v)).collect(Collectors.toList());
        List<Expr> bExprs = userVars.stream().map(v -> new ExprVar(v)).collect(Collectors.toList());
        GenericProblem<Map<Var, Var>, ?> result = new ProblemVarMappingExpr(aExprs, bExprs, Collections.emptyMap());
        return result;
    }




    public static Stream<ProblemNeighborhoodAware<Map<Var, Var>, Var>> createProblems(FeatureMap<Expr, Multimap<Expr, Expr>> cacheIndex, FeatureMap<Expr, Multimap<Expr, Expr>> queryIndex) {

        List<ProblemNeighborhoodAware<Map<Var, Var>, Var>> problems = new ArrayList<>();

        for(Entry<Set<Expr>, Collection<Multimap<Expr, Expr>>> entry : queryIndex.entrySet()) {
            Set<Expr> querySig = entry.getKey();
            Collection<Multimap<Expr, Expr>> queryMaps = entry.getValue();

            if(logger.isTraceEnabled()) { logger.trace("CAND LOOKUP with " + querySig); }


            // Base on the signatures: For the current query clause, find cache clauses that are less restrictive
            Collection<Entry<Set<Expr>, Multimap<Expr, Expr>>> cands = cacheIndex.getIfSubsetOf(querySig);

            // If there is none, the view is not suitable
            // If the cache index is empty, such as in the case of view := { ?s ?p ?o } then the view is
            // satisfiable after all
            if(cands.isEmpty()) {
                problems = Collections.singletonList(new ProblemStaticSolutions<>(Collections.singleton(null)));
                break;
            }

            for(Entry<Set<Expr>, Multimap<Expr, Expr>> e : cands) {
                Multimap<Expr, Expr> cacheMap = e.getValue();
                //System.out.println("  CACHE MAP: " + cacheMap);
                if(logger.isTraceEnabled()) { logger.trace("  CACHE MAP: " + cacheMap); }

                for(Multimap<Expr, Expr> queryMap : queryMaps) {
                    Map<Expr, Entry<Set<Expr>, Set<Expr>>> group = MapUtils.groupByKey(cacheMap.asMap(), queryMap.asMap());

                    Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> localProblems = group.values().stream()
                        .map(x -> {
                            Set<Expr> cacheExprs = x.getKey();
                            Set<Expr> queryExprs = x.getValue();
                            ProblemNeighborhoodAware<Map<Var, Var>, Var> p = new ProblemVarMappingExpr(cacheExprs, queryExprs, Collections.emptyMap());

                            if(logger.isTraceEnabled()) { logger.trace("Registered problem instance " + p + " with an estimated cost of " + p.getEstimatedCost()); }
                            if(logger.isTraceEnabled()) { logger.trace("  Enumerating its solutions yields " + p.generateSolutions().count() + " items: " + p.generateSolutions().collect(Collectors.toList())); }
//                            System.out.println("Registered problem instance " + p + " with an estimated cost of " + p.getEstimatedCost());
//                            System.out.println("  Enumerating its solutions yields " + p.generateSolutions().count() + " items: " + p.generateSolutions().collect(Collectors.toList()));

                            return p;
                        })
                        .collect(Collectors.toList());

                    problems.addAll(localProblems);
                }
            }

            //cands.forEach(x -> System.out.println("CAND: " + x.getValue()));
        }

        return problems.stream();
    }

    /**
     * TODO Return only the collection of problems at this stage
     *
     * @param cachePattern
     * @param queryPattern
     * @return
     */
    public static Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> createProblems(QuadFilterPatternCanonical cachePattern, QuadFilterPatternCanonical queryPattern) {
        FeatureMap<Expr, Multimap<Expr, Expr>> cacheIndex = AlgebraUtils.indexDnf(cachePattern.getFilterDnf());
        FeatureMap<Expr, Multimap<Expr, Expr>> queryIndex = AlgebraUtils.indexDnf(queryPattern.getFilterDnf());

        Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> result = new ArrayList<>();

        createProblems(cacheIndex, queryIndex).forEach(result::add);

        // TODO The code below does not correctly match quads:
        // Given a view (v1, {}) (i.e. no filters and a user query (u1, {u1.s = foo}),
        // we need to match u1 with any quad having a _subset_ of the constraints

        // Index the quads by the cnf (dnf)?

        IBiSetMultimap<Quad, Set<Set<Expr>>> cacheQuadIndex = AlgebraUtils.createMapQuadsToFilters(cachePattern);
        IBiSetMultimap<Quad, Set<Set<Expr>>> queryQuadIndex = AlgebraUtils.createMapQuadsToFilters(queryPattern);


        FeatureMap<Expr, Quad> queryQuadI = new FeatureMapImpl<>();
        queryQuadIndex.entries().forEach(e ->
            e.getValue().forEach(x -> queryQuadI.put(x, e.getKey())
        ));
//
//        FeatureMap<Expr, Quad> queryQuadI = new FeatureMapImpl<>();
//        queryQuadIndex.entries().forEach(e -> {
//            Set<Set<Expr>> cnf = e.getValue();
//            if(cnf.isEmpty()) {
//                cnf = Collections.singleton(Collections.emptySet());
//            } else {
//                cnf.forEach(x -> queryQuadI.put(x, e.getKey()));
//            }
//        });



        //cacheQuadIndex.getInverse().asMap().entrySet().forEach(e -> {
        for(Entry<Set<Set<Expr>>, Collection<Quad>> e : cacheQuadIndex.getInverse().asMap().entrySet()) {
            Collection<Quad> viewQuads = e.getValue();
            Set<Set<Expr>> dnf = e.getKey();

            // Small hack to look up query quads having more than 'none' constraints
//            if(exprs.isEmpty()) {
//                exprs = Collections.singleton(Collections.emptySet());
//            }

            Set<Quad> queryQuads = dnf.stream()
                .flatMap(clause -> queryQuadI.getIfSupersetOf(clause).stream().map(x -> x.getValue()))
                .collect(Collectors.toSet());

            ProblemVarMappingQuad quadProblem = new ProblemVarMappingQuad(viewQuads, queryQuads, Collections.emptyMap());

            if(logger.isTraceEnabled()) { logger.trace("Registered quad problem instance " + quadProblem + " with an estimated cost of " + quadProblem.getEstimatedCost()); }
            //System.out.println("Registered quad problem instance " + quadProblem + " with an estimated cost of " + quadProblem.getEstimatedCost());
            //System.out.println("Registered quad problem instance " + quadProblem + " with an estimated cost of " + quadProblem.getEstimatedCost());
            //System.out.println("  Enumerating its solutions yields " + quadProblem.generateSolutions().count());
            //System.out.println("Registered quad problem instance " + quadProblem + " with " + quadProblem.generateSolutions().count() + " solutions ");


            result.add(quadProblem);

        }

        if(false) {
        Map<Set<Set<Expr>>, Entry<Set<Quad>, Set<Quad>>> quadGroups = MapUtils.groupByKey(cacheQuadIndex.getInverse(), queryQuadIndex.getInverse());

        for(Entry<Set<Quad>, Set<Quad>> quadGroup : quadGroups.values()) {

            ProblemVarMappingQuad quadProblem = new ProblemVarMappingQuad(quadGroup.getKey(), quadGroup.getValue(), Collections.emptyMap());

            if(logger.isTraceEnabled()) { logger.trace("Registered quad problem instance " + quadProblem + " with an estimated cost of " + quadProblem.getEstimatedCost()); }

            //System.out.println("Registered quad problem instance " + quadProblem + " with an estimated cost of " + quadProblem.getEstimatedCost());
            //System.out.println("  Enumerating its solutions yields " + quadProblem.generateSolutions().count());
            //System.out.println("Registered quad problem instance " + quadProblem + " with " + quadProblem.generateSolutions().count() + " solutions ");


            result.add(quadProblem);
        }

        }

        return result;
    }

    public static Stream<Map<Var, Var>> solve(Collection<? extends ProblemNeighborhoodAware<Map<Var, Var>, Var>> problems) {

        Stream<Map<Var, Var>> result = ProblemContainerNeighbourhoodAware.solve(
                problems,
                Collections.emptyMap(),
                Map::keySet,
                MapUtils::mergeIfCompatible,
                Objects::isNull);

        return result;
    }


    public static Stream<Map<Var, Var>> createVarMapCandidates(QuadFilterPatternCanonical cachePattern, QuadFilterPatternCanonical queryPattern) {

        Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> problems = createProblems(cachePattern, queryPattern);
        Stream<Map<Var, Var>> result = solve(problems);

        return result;
    }
}
