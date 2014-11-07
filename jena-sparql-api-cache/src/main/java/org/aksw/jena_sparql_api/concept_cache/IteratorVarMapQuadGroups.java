package org.aksw.jena_sparql_api.concept_cache;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.AbstractIterator;
import com.hp.hpl.jena.sparql.core.Var;

class IteratorVarMapQuadGroups
    extends AbstractIterator<Map<Var, Var>>
{
    private Iterator<List<Map<Var, Var>>> itSolutionParts;

    public IteratorVarMapQuadGroups(Iterator<List<Map<Var, Var>>> itSolutionParts) {
        this.itSolutionParts = itSolutionParts;
    }

    @Override
    protected Map<Var, Var> computeNext() {
        Map<Var, Var> result = null;

        while(itSolutionParts.hasNext()) {
            List<Map<Var, Var>> cand = itSolutionParts.next();
            result = ConceptMap.mergeCompatible(cand);

            if(result != null) {
                break;
            }
        }

        if(result == null) {
            result = endOfData();
        }

        return result;
    }
}