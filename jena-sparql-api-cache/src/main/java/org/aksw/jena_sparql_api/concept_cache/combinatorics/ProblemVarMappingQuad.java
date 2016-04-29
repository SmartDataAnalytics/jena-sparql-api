package org.aksw.jena_sparql_api.concept_cache.combinatorics;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.aksw.isomorphism.Problem;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;

public class ProblemVarMappingQuad
    extends ProblemMappingEquivBase<Map<Var, Var>, Quad, Quad>
{
    public ProblemVarMappingQuad(Collection<Quad> as, Collection<Quad> bs) {
        super(as, bs);
    }

    @Override
    public Stream<Map<Var, Var>> generateSolutions(Map<Var, Var> baseVarMap) {
        Iterable<Map<Var, Var>> tmp = CombinatoricsUtils.createSolutions(as, bs, baseVarMap);
        Stream<Map<Var, Var>> result = StreamSupport.stream(tmp.spliterator(), false);
        return result;
    }

    @Override
    public Collection<Problem<Map<Var, Var>>> refine(Map<Var, Var> partialSolution) {
        // TODO Implement me
        return Collections.singleton(this);
    }
}
