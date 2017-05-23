package org.aksw.jena_sparql_api.concept_cache.combinatorics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;

class IterableVarMapQuadGroup
    implements Iterable<Map<Var, Var>>
{
    private List<Quad> sourceQuads;
    private List<Quad> targetQuads;

    private Map<Var, Var> baseSolution;

    public IterableVarMapQuadGroup(List<Quad> sourceQuads, List<Quad> targetQuads, Map<Var, Var> baseSolution) {
        this.sourceQuads = sourceQuads;
        this.targetQuads = targetQuads;
        this.baseSolution = baseSolution;
    }

    @Override
    public Iterator<Map<Var, Var>> iterator() {
        Iterator<Map<Var, Var>> result = new IteratorVarMapQuadGroup(sourceQuads, targetQuads, baseSolution);
        return result;
    }

    public static Iterable<Map<Var, Var>> create(Entry<? extends Collection<Quad>, ? extends Collection<Quad>> quadGroup, Map<Var, Var> baseSolution) {
        Iterable<Map<Var, Var>> result = new IterableVarMapQuadGroup(new ArrayList<Quad>(quadGroup.getKey()), new ArrayList<Quad>(quadGroup.getValue()), baseSolution);
        return result;
    }

}