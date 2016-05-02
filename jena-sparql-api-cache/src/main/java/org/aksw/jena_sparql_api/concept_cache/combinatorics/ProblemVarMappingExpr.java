package org.aksw.jena_sparql_api.concept_cache.combinatorics;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import org.aksw.isomorphism.Problem;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprLib;
import org.apache.jena.sparql.expr.ExprSystem;

/**
 * Match two clauses of expressions against each other
 *
 * @author raven
 *
 */
public class ProblemVarMappingExpr
    extends ProblemMappingEquivBase<Map<Var, Var>, Collection<Expr>, Collection<Expr>>
{


    public ProblemVarMappingExpr(Collection<Collection<Expr>> as,
            Collection<Collection<Expr>> bs) {
        super(as, bs);
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
