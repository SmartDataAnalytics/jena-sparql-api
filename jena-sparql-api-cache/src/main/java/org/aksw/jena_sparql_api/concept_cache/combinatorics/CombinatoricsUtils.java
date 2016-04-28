package org.aksw.jena_sparql_api.concept_cache.combinatorics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.aksw.commons.collections.CartesianProduct;
import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.aksw.jena_sparql_api.concept_cache.core.SparqlCacheUtils;
import org.aksw.jena_sparql_api.concept_cache.domain.PatternSummary;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;

public class CombinatoricsUtils {

    public static Stream<Map<Var, Var>> computeVarMapQuadBased(PatternSummary needle, PatternSummary haystack, Set<Set<Var>> candVarCombos) {
        Stream<Map<Var, Var>> result = computeVarMapQuadBased(needle.getQuadToCnf(), haystack.getQuadToCnf(), candVarCombos);
        return result;
    }

    /**
     * TODO the quad groups are equivalent classes - this seems to be in essence what JgraphT isomorphims tooling does
     * http://jgrapht.org/javadoc/org/jgrapht/alg/isomorphism/VF2GraphIsomorphismInspector.html (and SubGraphIsomorphism) variant
     *
     * Find a mapping of variables from cand to query, such that the pattern of
     * cand becomes a subset of that of query
     *
     * null if no mapping can be established
     *
     * @param query
     * @param cand
     * @return
     */
    public static Stream<Map<Var, Var>> computeVarMapQuadBased(IBiSetMultimap<Quad, Set<Set<Expr>>> queryQuadToCnf, IBiSetMultimap<Quad, Set<Set<Expr>>> candQuadToCnf, Set<Set<Var>> candVarCombos) {

        IBiSetMultimap<Set<Set<Expr>>, Quad> cnfToCandQuad = candQuadToCnf.getInverse();
        IBiSetMultimap<Set<Set<Expr>>, Quad> cnfToQueryQuad = queryQuadToCnf.getInverse();

        //IBiSetMultimap<Quad, Quad> candToQuery = new BiHashMultimap<Quad, Quad>();
//        Map<Set<Set<Expr>>, QuadGroup> cnfToQuadGroup = new HashMap<Set<Set<Expr>>, QuadGroup>();


        // TODO Replace quad group by a pair object
        // Note: quad groups are equivalence classes
        List<QuadGroup> quadGroups = new ArrayList<QuadGroup>();
        for(Entry<Set<Set<Expr>>, Collection<Quad>> entry : cnfToCandQuad.asMap().entrySet()) {

            //Quad candQuad = entry.getKey();
            Set<Set<Expr>> cnf = entry.getKey();

            Collection<Quad> candQuads = entry.getValue();
            Collection<Quad> queryQuads = cnfToQueryQuad.get(cnf);

            if(queryQuads.isEmpty()) {
                return Collections.<Map<Var, Var>>emptySet().stream();
            }

            QuadGroup quadGroup = new QuadGroup(candQuads, queryQuads);
            quadGroups.add(quadGroup);

            // TODO We now have grouped together quad having the same constraint summary
            // Can we derive some additional constraints form the var occurrences?
        }

        // Order the equivalence classes by the number of possible combinations
        // - least number of candidates first
        Collections.sort(quadGroups, new Comparator<QuadGroup>() {
            @Override
            public int compare(QuadGroup a, QuadGroup b) {
                int i = Utils2.getNumMatches(a);
                int j = Utils2.getNumMatches(b);
                int r = i - j;
                return r;
            }
        });


        List<Iterable<Map<Var, Var>>> cartesian = new ArrayList<Iterable<Map<Var, Var>>>(quadGroups.size());

        // TODO Somehow obtain a base mapping (is that even possible?)
        Map<Var, Var> baseMapping = Collections.<Var, Var>emptyMap();

        // Create a cartesian product over all solutions of the equivalence classes
        for(QuadGroup quadGroup : quadGroups) {
            Iterable<Map<Var, Var>> it = IterableVarMapQuadGroup.create(quadGroup, baseMapping);
            cartesian.add(it);
        }

        CartesianProduct<Map<Var, Var>> cart = new CartesianProduct<Map<Var,Var>>(cartesian);


        // Combine the solutions of each equivalence class into an overall solution,
        // thereby filter out incompatible bindings (indicated by null)
        Stream<Map<Var, Var>> result = cart.stream()
            .map(solutionParts -> SparqlCacheUtils.mergeCompatible(solutionParts))
            .filter(Objects::nonNull);

        return result;
    }
}



//SetMultimap<Quad, Quad> summaryToQuadsCand = quadJoinSummary(new ArrayList<Quad>(candQuads));
//System.out.println("JoinSummaryCand: " + summaryToQuadsCand);
//
//SetMultimap<Quad, Quad> summaryToQuadsQuery = quadJoinSummary(new ArrayList<Quad>(queryQuads));
//System.out.println("JoinSummaryQuery: " + summaryToQuadsQuery);
//
//for(Entry<Quad, Collection<Quad>> candEntry : summaryToQuadsCand.asMap().entrySet()) {
//  queryQuads = summaryToQuadsQuery.get(candEntry.getKey());
//
//  // TODO What if the mapping is empty?
//  QuadGroup group = new QuadGroup(candEntry.getValue(), queryQuads);
//
//  cnfToQuadGroup.put(cnf, group);
//}
