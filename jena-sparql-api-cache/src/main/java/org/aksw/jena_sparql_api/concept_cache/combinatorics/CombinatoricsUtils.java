package org.aksw.jena_sparql_api.concept_cache.combinatorics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collections.CartesianProduct;
import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.aksw.jena_sparql_api.concept_cache.domain.PatternSummary;

import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;

public class CombinatoricsUtils {

    public static Iterator<Map<Var, Var>> computeVarMapQuadBased(PatternSummary needle, PatternSummary haystack, Set<Set<Var>> candVarCombos) {
        Iterator<Map<Var, Var>> result = computeVarMapQuadBased(needle.getQuadToCnf(), haystack.getQuadToCnf(), candVarCombos);
        return result;
    }

    /**
     * Find a mapping of variables from cand to query, such that the pattern of
     * cand becomes a subset of that of query
     *
     * null if no mapping can be established
     *
     * @param query
     * @param cand
     * @return
     */
    public static Iterator<Map<Var, Var>> computeVarMapQuadBased(IBiSetMultimap<Quad, Set<Set<Expr>>> queryQuadToCnf, IBiSetMultimap<Quad, Set<Set<Expr>>> candQuadToCnf, Set<Set<Var>> candVarCombos) {

        IBiSetMultimap<Set<Set<Expr>>, Quad> cnfToCandQuad = candQuadToCnf.getInverse();
        IBiSetMultimap<Set<Set<Expr>>, Quad> cnfToQueryQuad = queryQuadToCnf.getInverse();

        //IBiSetMultimap<Quad, Quad> candToQuery = new BiHashMultimap<Quad, Quad>();
//        Map<Set<Set<Expr>>, QuadGroup> cnfToQuadGroup = new HashMap<Set<Set<Expr>>, QuadGroup>();
        List<QuadGroup> quadGroups = new ArrayList<QuadGroup>();
        for(Entry<Set<Set<Expr>>, Collection<Quad>> entry : cnfToCandQuad.asMap().entrySet()) {

            //Quad candQuad = entry.getKey();
            Set<Set<Expr>> cnf = entry.getKey();

            Collection<Quad> candQuads = entry.getValue();
            Collection<Quad> queryQuads = cnfToQueryQuad.get(cnf);

            if(queryQuads.isEmpty()) {
                return Collections.<Map<Var, Var>>emptySet().iterator();
            }

            QuadGroup quadGroup = new QuadGroup(candQuads, queryQuads);
            quadGroups.add(quadGroup);

            // TODO We now have grouped together quad having the same constraint summary
            // Can we derive some additional constraints form the var occurrences?


//            SetMultimap<Quad, Quad> summaryToQuadsCand = quadJoinSummary(new ArrayList<Quad>(candQuads));
//            System.out.println("JoinSummaryCand: " + summaryToQuadsCand);
//
//            SetMultimap<Quad, Quad> summaryToQuadsQuery = quadJoinSummary(new ArrayList<Quad>(queryQuads));
//            System.out.println("JoinSummaryQuery: " + summaryToQuadsQuery);
//
//            for(Entry<Quad, Collection<Quad>> candEntry : summaryToQuadsCand.asMap().entrySet()) {
//                queryQuads = summaryToQuadsQuery.get(candEntry.getKey());
//
//                // TODO What if the mapping is empty?
//                QuadGroup group = new QuadGroup(candEntry.getValue(), queryQuads);
//
//                cnfToQuadGroup.put(cnf, group);
//            }
        }

        // Figure out which quads have ambiguous mappings

//        for(Entry<Set<Set<Expr>>, QuadGroup>entry : cnfToQuadGroup.entrySet()) {
//            System.out.println(entry.getKey() + ": " + entry.getValue());
//        }

        // Order the quad groups by number of candidates - least number of candidates first
//        List<QuadGroup> quadGroups = new ArrayList<QuadGroup>(cnfToQuadGroup.values());
        Collections.sort(quadGroups, new Comparator<QuadGroup>() {
            @Override
            public int compare(QuadGroup a, QuadGroup b) {
                int i = Utils2.getNumMatches(a);
                int j = Utils2.getNumMatches(b);
                int r = j - i;
                return r;
            }
        });


        List<Iterable<Map<Var, Var>>> cartesian = new ArrayList<Iterable<Map<Var, Var>>>(quadGroups.size());

        // TODO Somehow obtain a base mapping
        Map<Var, Var> baseMapping = Collections.<Var, Var>emptyMap();

        for(QuadGroup quadGroup : quadGroups) {
            Iterable<Map<Var, Var>> it = IterableVarMapQuadGroup.create(quadGroup, baseMapping);
            cartesian.add(it);
        }

        CartesianProduct<Map<Var, Var>> cart = new CartesianProduct<Map<Var,Var>>(cartesian);

        Iterator<Map<Var, Var>> result = new IteratorVarMapQuadGroups(cart.iterator());

        return result;
    }
}
