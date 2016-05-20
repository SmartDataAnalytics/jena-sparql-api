package org.aksw.jena_sparql_api.concept_cache.combinatorics;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.aksw.commons.collections.MapUtils;
import org.aksw.isomorphism.IsoMapUtils;
import org.aksw.isomorphism.Problem;
import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.FunctionLabel;
import org.apache.jena.sparql.expr.NodeValue;


/**
 * Match two clauses of expressions against each other
 *
 * All expressions of the first set must match to expressions of the second set
 *
 * @author raven
 *
 */
public class ProblemVarMappingExpr
    extends ProblemMappingEquivBase<Collection<Expr>, Collection<Expr>, Var, Var>
{


    public ProblemVarMappingExpr(Collection<? extends Collection<Expr>> as,
            Collection<? extends Collection<Expr>> bs, Map<Var, Var> baseSolution) {
        super(as, bs, baseSolution);
    }

    @Override
    public Stream<Map<Var, Var>> generateSolutions() {

        Map<Var, Var> baseSolution = Collections.emptyMap();
        Stream<Map<Var, Var>> result = IsoMapUtils.createSolutionStream(
                as,
                bs,
                (x, y) -> createVarMap(x, y),
                baseSolution);

        return result;
    }

    @Override
    public Collection<Problem<Map<Var, Var>>> refine(Map<Var, Var> partialSolution) {
        return Collections.singleton(this);
    }

    public static Iterable<Map<Var, Var>> createSolutions(Collection<Expr> as, Collection<Expr> bs) {
        Map<Var, Var> baseSolution = Collections.emptyMap();
        Iterable<Map<Var, Var>> result =
                () -> IsoMapUtils.createSolutionStream(
                    as,
                    bs,
                    ProblemVarMappingExpr::createSingleVarMap,
                    baseSolution).iterator();

        return result;
    }

    public static Map<Var, Var> createVarMap(Collection<Expr> as, Collection<Expr> bs) {
        Map<Var, Var> baseVarMap = Collections.emptyMap();

        // TODO Not finished
        return baseVarMap;
    }

    public static Map<Var, Var> createSingleVarMap(Expr a, Expr b) {
        Map<Var, Var> result = createVarMap(a, b).findFirst().map(x -> x.getVarMap()).orElse(null);
        return result;
    }

    public static Stream<ExprMapSolution> createVarMap(Expr a, Expr b) {
        Stream<ExprMapSolution> result = createVarMap(a, b, Collections.emptyMap());
        return result;
    }

    /**
     *
     * It is important to note that linearization causes the leaf nodes to be
     * matched first. Hence, inner nodes are only processed once variable mappings
     * have been obtained from the map.
     *
     * @param a
     * @param b
     * @return
     */
    public static Stream<ExprMapSolution> createVarMap(Expr a, Expr b, Map<Var, Var> baseSolution) {
        List<Expr> as = ExprUtils.linearizePrefix(a, Collections.singleton(null)).collect(Collectors.toList());
        List<Expr> bs = ExprUtils.linearizePrefix(b, Collections.singleton(null)).collect(Collectors.toList());

        // Get the number of leafs of b
        //int n = ExprUtils.countLeafs(a);
        //int m = ExprUtils.countLeafs(b);
        int n = as.size();
        int m = bs.size();

        // If there is a match, we can continue by the size of m, as there cannot be another overlap
        //Collection<ExprMapSolution> result = new ArrayList<>();

//      IntStream.range(0, n - m).forEach(i -> {
//      });

        //for(int i = 0; i < n - m + 1; ++i) {
        Stream<ExprMapSolution> result = IntStream.range(0, n - m + 1)
            .mapToObj(i -> {
                Map<Var, Var> varMap = new HashMap<Var, Var>(baseSolution);
                Expr ae = null;
                for(int j = 0; j < m; ++j) {
                //Stream<ExprMapSolution> r = IntStream.range(0, m).map(j -> {
                    ae = as.get(i + j);
                    Expr be = bs.get(j);
                    boolean isCompatible;
                    if(ae == null && be == null) {
                        isCompatible = true;
                    }
                    else if(ae == null || be == null) {
                        isCompatible = false;
                    }
                    else if(ae.isVariable() && be.isVariable()) {
                        Var av = ae.getExprVar().asVar();
                        Var bv = be.getExprVar().asVar();
                        // Add a mapping to varMap if it is compatible
                        Map<Var, Var> tmp = Collections.singletonMap(av, bv);
                        isCompatible = MapUtils.isCompatible(tmp, varMap);

                        varMap.putAll(tmp);
                    }
                    else if(ae.isConstant() && be.isConstant()) {
                        NodeValue ac = ae.getConstant();
                        NodeValue bc = be.getConstant();

                        isCompatible = ac.equals(bc);
                    }
                    else if(ae.isFunction() && be.isFunction()) {
                        // The function symbols must match - the sub-expressions were already matched
                        // TODO Deal with sparql EXIST
                        ExprFunction af = ae.getFunction();
                        ExprFunction bf = be.getFunction();

                        FunctionLabel al = af.getFunctionSymbol();
                        FunctionLabel bl = bf.getFunctionSymbol();

                        isCompatible = al.equals(bl);
                    }
                    else {
                        isCompatible = false;
                    }

                    if(!isCompatible) {
                        varMap = null;
                        break;
                    }
                }

                ExprMapSolution r = varMap == null
                        ? null
                        : new ExprMapSolution(varMap, b, a, ae);

                return r;
            })
            .filter(x -> !Objects.isNull(x))
            ;

        return result;
    }


}
