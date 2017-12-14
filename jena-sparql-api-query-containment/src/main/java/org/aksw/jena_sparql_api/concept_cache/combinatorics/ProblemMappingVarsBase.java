package org.aksw.jena_sparql_api.concept_cache.combinatorics;

import java.util.Collection;
import java.util.Map;

import org.aksw.combinatorics.solvers.ProblemMappingKPermutationsOfN;

public abstract class ProblemMappingVarsBase<A, B, X, Y>
    extends ProblemMappingKPermutationsOfN<A, B, X, Y>
{

    public ProblemMappingVarsBase(Collection<A> as,
            Collection<B> bs, Map<X, Y> baseSolution) {
        super(as, bs, baseSolution);
    }

}
