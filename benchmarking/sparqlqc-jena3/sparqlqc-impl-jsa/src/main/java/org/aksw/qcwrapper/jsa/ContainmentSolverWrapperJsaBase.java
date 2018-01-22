package org.aksw.qcwrapper.jsa;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.algebra.utils.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.concept_cache.core.SparqlQueryContainmentUtils;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;

public class ContainmentSolverWrapperJsaBase
	extends ContainmentSolverWrapperBase
{
    protected BiFunction<QuadFilterPatternCanonical, QuadFilterPatternCanonical, Stream<Map<Var, Var>>> qfpcMatcher;

    public ContainmentSolverWrapperJsaBase(
            BiFunction<QuadFilterPatternCanonical, QuadFilterPatternCanonical, Stream<Map<Var, Var>>> qfpcMatcher) {
        super();
        this.qfpcMatcher = qfpcMatcher;
    }

    @Override
    public boolean entailed(Query q1, Query q2) {// throws ContainmentTestException {
        boolean result = SparqlQueryContainmentUtils.tryMatch(q2, q1, qfpcMatcher);
        return result;
    }

}
