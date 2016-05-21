package org.aksw.isomorphism;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.concept_cache.combinatorics.CombinatoricsUtils;
import org.aksw.jena_sparql_api.concept_cache.combinatorics.ProblemVarMappingExpr;
import org.aksw.jena_sparql_api.concept_cache.combinatorics.ProblemVarMappingQuad;
import org.aksw.jena_sparql_api.concept_cache.core.SparqlCacheUtils;
import org.aksw.jena_sparql_api.utils.CnfUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.state_space_search.core.SearchUtils;
import org.aksw.state_space_search.core.StateSearchUtils;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.util.ExprUtils;

public class TestStateSpaceSearch {
    public static void main(String[] args) {
        Expr a = ExprUtils.parse("(?z = ?x + 1)");
        Expr b = ExprUtils.parse("?a = ?b || (?c = ?a + 1) && (?k = ?i + 1)");
        //Expr b = ExprUtils.parse("?x = ?y || (?z = ?x + 1)");

        Set<Set<Expr>> ac = CnfUtils.toSetCnf(b);
        Set<Set<Expr>> bc = CnfUtils.toSetCnf(a);

        Problem<Map<Var, Var>> p = new ProblemVarMappingExpr(ac, bc, Collections.emptyMap());

        System.out.println("p");
        System.out.println(p.getEstimatedCost());
        ProblemVarMappingExpr.createVarMap(a, b).forEach(x -> System.out.println(x));

        Collection<Quad> as = Arrays.asList(new Quad(Vars.g, Vars.s, Vars.p, Vars.o));
        Collection<Quad> bs = Arrays.asList(new Quad(Vars.l, Vars.x, Vars.y, Vars.z));


        //Collection<Quad> cq =
        System.out.println("q");
        Problem<Map<Var, Var>> q = new ProblemVarMappingQuad(as, bs, Collections.emptyMap());
        System.out.println(q.getEstimatedCost());

        q.generateSolutions().forEach(x -> System.out.println(x));


        //Maps.com

        System.out.println("pc");
        ProblemContainerImpl<Map<Var, Var>> pc = ProblemContainerImpl.create(p, q);
        StateProblemContainer<Map<Var, Var>> state = new StateProblemContainer<>(Collections.emptyMap(), pc, SparqlCacheUtils::mergeCompatible);
        //SearchUtils.depthFirstSearch(state, isFinal, vertexToResult, vertexToEdges, edgeCostComparator, edgeToTargetVertex, depth, maxDepth)
        StateSearchUtils.depthFirstSearch(state, 10).forEach(x -> System.out.println(x));


        //p.generateSolutions().forEach(x -> System.out.println(x));
    }
}
