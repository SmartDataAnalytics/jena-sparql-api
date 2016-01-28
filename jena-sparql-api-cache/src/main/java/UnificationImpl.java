import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.utils.ExprListUtils;
import org.aksw.jena_sparql_api.utils.QuadUtils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadBlock;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_Function;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVars;

class UnificationResult {
    protected Map<Var, Var> varMap;

}

/**
 * Maybe we could provide an expr view over ops?
 *
 */
//class ExprFunctionOp
//    extends ExprFunction
//{
//
//    protected ExprFunctionOp(String fName) {
//        super(fName);
//        // TODO Auto-generated constructor stub
//    }
//
//
//}


/**
 * Next issue: we should retain order of elements whenever possible.
 * E.g. when removing duplicates, it should be the first encountered element that
 * should be retained.
 *
 * @author raven
 *
 */
class OpToExprUtils
{
    public static List<Expr> toExprs(OpQuadBlock op) {
        QuadPattern quadPatetrn = op.getPattern();
        List<Expr> result = toExprs(quadPatetrn);
        return result;
    }

    public static List<Expr> toExprs(QuadPattern quadPattern) {
        List<Expr> result = new ArrayList<Expr>();
        for(Quad quad : quadPattern) {
            Expr e = toExpr(quad);
            result.add(e);
        }
        return result;
    }

    public static Expr toExpr(Quad quad) {
        ExprList args = ExprListUtils.nodesToExprs(QuadUtils.quadToList(quad));
        Expr result = new E_Function(UnificationImpl.QUAD_IRI, args);
        return result;
    }



}

public class UnificationImpl {
    public static final String QUAD_IRI = "http://example.org/quad";


    public static Multimap<Var, Expr> indexEnvironments(Iterable<Expr> exprs) {
        Multimap<Var, Expr> result = HashMultimap.create();
        for(Expr expr : exprs) {
            Set<Var> vars = ExprVars.getVarsMentioned(expr);
            for(Var var : vars) {
                result.put(var, expr);
            }
        }

        return result;
    }

    /**
     * Get the environment of a variable 'var' - this is the set of all
     * expressions that mention it.
     * @param exprs
     * @param var
     * @return
     */
    public static Set<Expr> getEnvironment(List<Expr> exprs, Var var) {
        Set<Expr> result = new HashSet<Expr>();
        for(Expr expr : exprs) {
            //PatternVars.
            Set<Var> vars = ExprVars.getVarsMentioned(expr);
            if(vars.contains(var)) {
                result.add(expr);
            }
        }
        return result;
    }


    public static void main(String[] test) {
        // So the basic idea is, to transform the whole algebra into a uniform expression -
        // i.e. get rid of the distinction between Ops and Exprs
        // the main motivation is to have triple patterns and exprs in the same set


    }

    /**
     *
     * @param aCnf
     * @param bCnf
     */
    public UnificationResult tryUnifyWithEquivalences(Expr a, Expr b, Object candidates) {
//        List<Expr> as = generateEquivalences(a);
//        List<Expr> bs = generateEquivalences(b);

        // Next we should cluster the equivalences




        return new UnificationResult();
    }


    public UnificationResult tryUnify(List<Expr> as, List<Expr> bs, Object candidates) {
        // for each cluster ...
        int m = as.size();
        int n = bs.size();
        for(int i = 0; i < m; ++i) {
            for(int j = 0; j < n; ++j) {
                Expr ea = as.get(i);
                Expr eb = bs.get(j);

                // try to unify (without generating new equivalences)
                //tryUnify(ea, eb, candidates);
            }
        }
        return null;
    }


    public UnificationResult tryUnify(Expr a, Expr b, Map<Var, Var> varMap) {
        if(a.isFunction() && b.isFunction()) {
            ExprFunction fa = a.getFunction();
            ExprFunction fb = b.getFunction();

            tryUnify(fa, fb, varMap);

        } else if(a.isVariable() && b.isVariable()) {
            Var va = a.asVar();
            Var vb = b.asVar();

            varMap.put(vb, va);
        } else if(a.isConstant() && b.isConstant()) {
            boolean isEqual = a.equals(b);
            // TODO indicate an empty mapping
        }

        return null;
    }

    public UnificationResult tryUnify(Var a, Var b, Map<Var, Var> base) {
        return null;
    }

    public UnificationResult tryUnify(ExprFunction a, ExprFunction b, Map<Var, Var> base) {
        Map<Var, Var> map = new HashMap<Var, Var>(base);

        UnificationResult result;
        boolean examine = a.getFunctionSymbol().equals(b.getFunctionSymbol());
        List<Expr> as = a.getArgs();
        List<Expr> bs = b.getArgs();
        examine = examine && as.size() == bs.size();
        if(examine) {
            int n = as.size();
            for(int i = 0; i < n; ++i) {
                Expr ai = as.get(i);
                Expr bi = bs.get(i);

                // Now we need to try to unify these expressions again
            }

            result = null;
        } else {
            result = null;
        }

        return result;
    }

    /**
     * For the sake of a uniform representation, we convert quads to exprs:
     * (g, s, p, o) -> quad(g, s, p, o)
     * @param quad
     * @return
     */
    public static Expr quadToExpr(Quad quad) {
        ExprList exprList = ExprListUtils.nodesToExprs(QuadUtils.quadToList(quad));
        E_Function result = new E_Function(QUAD_IRI, exprList);
        return result;
    }

    //public static groupClausesByType(Expr exp)

    /**
     * equals(a, b) -> { equals(a, b), equals(b, a) }
     *
     *
     *
     * @param expr
     * @return
     */
    public static Set<Expr> generateEquivalences(Expr expr) {
        Set<Expr> result = new HashSet<Expr>();
        result.add(expr);

        if(expr instanceof E_Equals) {
            E_Equals e = (E_Equals)expr;
            E_Equals x = new E_Equals(e.getArg2(), e.getArg1());
            result.add(x);
        }

        return result;
    }
}
