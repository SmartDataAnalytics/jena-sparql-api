package org.aksw.jena_sparql_api.concept_cache.combinatorics;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import org.aksw.isomorphism.Problem;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;

/**
 * Match two clauses of expressions against each other
 *
 * @author raven
 *
 */
public class ProblemVarMappingExpr
    extends ProblemMappingEquivBase<Collection<Expr>, Collection<Expr>, Var, Var>
{


    public ProblemVarMappingExpr(Collection<Collection<Expr>> as,
            Collection<Collection<Expr>> bs, Map<Var, Var> baseSolution) {
        super(as, bs, baseSolution);
    }

    @Override
    public Stream<Map<Var, Var>> generateSolutions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Problem<Map<Var, Var>>> refine(
            Map<Var, Var> partialSolution) {
        // TODO Auto-generated method stub
        return null;
    }


}
