package org.aksw.jena_sparql_api.concept_cache.combinatorics;

import java.util.Collection;
import java.util.Map;

import org.aksw.isomorphism.ProblemNeighborhoodAware;

public abstract class ProblemMappingVarsBase<A, B, X, Y>
    extends ProblemMappingKPermutationsOfN<A, B, X, Y>
{

    public ProblemMappingVarsBase(Collection<? extends A> as,
            Collection<? extends B> bs, Map<X, Y> baseSolution) {
        super(as, bs, baseSolution);
    }

}
