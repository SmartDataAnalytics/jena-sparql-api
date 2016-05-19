package org.aksw.isomorphism;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.concept_cache.combinatorics.ProblemVarMappingExpr;
import org.aksw.jena_sparql_api.utils.CnfUtils;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.util.ExprUtils;

public class TestStateSpaceSearch {
    public static void main(String[] args) {
        Expr a = ExprUtils.parse("?a = ?b || (?c = ?a + 1)");
        Expr b = ExprUtils.parse("?x = ?y || (?z = ?x + 1)");

        Set<Set<Expr>> ac = CnfUtils.toSetCnf(a);
        Set<Set<Expr>> bc = CnfUtils.toSetCnf(b);

        Problem<Map<Var, Var>> p = new ProblemVarMappingExpr(ac, bc, Collections.emptyMap());



        ProblemContainer<Map<Var, Var>> pc = ProblemContainerImpl.create();

        Stream<Map<Var, Var>> r = ProblemVarMappingExpr.createVarMap(a, b);



        r.forEach(x -> System.out.println(x));

        //p.generateSolutions().forEach(x -> System.out.println(x));
    }
}
