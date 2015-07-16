package org.aksw.jena_sparql_api.update;

import java.util.Set;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;

import com.google.common.base.Function;
import com.hp.hpl.jena.sparql.core.Quad;

public class FunctionQuadDiffUnique
    implements Function<Diff<? extends Iterable<Quad>>, Diff<Set<Quad>>>
{
    private QueryExecutionFactory qef;
    private QuadContainmentChecker containmentChecker;

    public FunctionQuadDiffUnique(QueryExecutionFactory qef, QuadContainmentChecker containmentChecker) {
        this.qef = qef;
        this.containmentChecker = containmentChecker;
    }

    @Override
    public Diff<Set<Quad>> apply(Diff<? extends Iterable<Quad>> diff) {
        Diff<Set<Quad>> result = UpdateUtils.makeUnique(diff, qef, containmentChecker);
        return result;
    }

    public static FunctionQuadDiffUnique create(QueryExecutionFactory qef, QuadContainmentChecker containmentChecker) {
        FunctionQuadDiffUnique result = new FunctionQuadDiffUnique(qef, containmentChecker);
        return result;
    }
}