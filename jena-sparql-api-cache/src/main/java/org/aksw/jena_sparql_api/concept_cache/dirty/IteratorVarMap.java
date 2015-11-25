package org.aksw.jena_sparql_api.concept_cache.dirty;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.aksw.jena_sparql_api.concept_cache.combinatorics.QuadGroup;
import org.aksw.jena_sparql_api.concept_cache.domain.PatternSummary;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;

import com.google.common.collect.AbstractIterator;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.graph.NodeTransform;

class IteratorVarMap
    extends AbstractIterator<Map<Var, Var>>
{
    private Set<Var> candVarCombo;
    private List<QuadGroup> quadGroups;
    private IBiSetMultimap<Var, Var> candToQuery;

    private PatternSummary cand;
    private PatternSummary query;


    private Set<Set<Var>> open;

    public IteratorVarMap() {

    }

    @Override
    protected Map<Var, Var> computeNext() {

        while(!open.isEmpty()) {
            Set<Var> start = open.iterator().next();


        }



        Map<Var, Var> varMap = null;

        // TODO Auto-generated method stub
        return null;
    }

    public static boolean isSubsumed(QuadFilterPatternCanonical source, QuadFilterPatternCanonical target, NodeTransform nodeTransform) {
        QuadFilterPatternCanonical mapped = source.applyNodeTransform(nodeTransform);
        boolean result = mapped.isSubsumedBy(target);
        return result;
    }


    public static boolean isEquals(QuadFilterPatternCanonical source, QuadFilterPatternCanonical target, NodeTransform nodeTransform) {
        QuadFilterPatternCanonical mapped = source.applyNodeTransform(nodeTransform);
        boolean result = target.equals(mapped);
        return result;
    }
}