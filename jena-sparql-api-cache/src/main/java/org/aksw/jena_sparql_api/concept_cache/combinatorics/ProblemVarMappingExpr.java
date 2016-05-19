package org.aksw.jena_sparql_api.concept_cache.combinatorics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collections.MapUtils;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Problem<Map<Var, Var>>> refine(
            Map<Var, Var> partialSolution) {

        // TODO Auto-generated method stub
        return null;
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
    public Stream<Map<Var, Var>> createVarMap(Expr a, Expr b) {
        List<Expr> as = ExprUtils.linearizePrefix(a, null).collect(Collectors.toList());
        List<Expr> bs = ExprUtils.linearizePrefix(b, null).collect(Collectors.toList());

        // Get the number of leafs of b
        //int n = ExprUtils.countLeafs(a);
        //int m = ExprUtils.countLeafs(b);
        int n = as.size();
        int m = bs.size();

        // If there is a match, we can continue by the size of m, as there cannot be another overlap
        Collection<Map<Var, Var>> result = new ArrayList<>();

//      IntStream.range(0, n - m).forEach(i -> {
//      });

        for(int i = 0; i < n - m; ++i) {
            Expr ae = as.get(i);
            Map<Var, Var> varMap = new HashMap<Var, Var>();
            for(int j = 0; j < m; ++j) {
                Expr be = bs.get(j);
                boolean isCompatible;
                if(ae.isVariable() && be.isVariable()) {
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
                    FunctionLabel bl = af.getFunctionSymbol();

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

            if(varMap != null) {
                result.add(varMap);
            }
        }

        return result.stream();
    }


}
