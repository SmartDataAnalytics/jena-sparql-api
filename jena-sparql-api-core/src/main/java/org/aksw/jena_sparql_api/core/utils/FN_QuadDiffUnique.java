package org.aksw.jena_sparql_api.core.utils;

import java.util.Set;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.jena_sparql_api.core.QuadContainmentChecker;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;

import com.google.common.base.Function;
import com.hp.hpl.jena.sparql.core.Quad;

public class FN_QuadDiffUnique
    implements Function<Diff<? extends Iterable<Quad>>, Diff<Set<Quad>>>
{
    private QueryExecutionFactory qef;
    private QuadContainmentChecker containmentChecker;

    public FN_QuadDiffUnique(QueryExecutionFactory qef, QuadContainmentChecker containmentChecker) {
        this.qef = qef;
        this.containmentChecker = containmentChecker;
    }

    @Override
    public Diff<Set<Quad>> apply(Diff<? extends Iterable<Quad>> diff) {
        Diff<Set<Quad>> result = UpdateDiffUtils.makeUnique(diff, qef, containmentChecker);
        return result;
    }

    public static FN_QuadDiffUnique create(QueryExecutionFactory qef, QuadContainmentChecker containmentChecker) {
        FN_QuadDiffUnique result = new FN_QuadDiffUnique(qef, containmentChecker);
        return result;
    }
}