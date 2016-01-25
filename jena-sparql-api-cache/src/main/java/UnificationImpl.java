import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.utils.ExprListUtils;
import org.aksw.jena_sparql_api.utils.QuadUtils;

import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_Function;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.ExprList;

class UnificationResult {
    protected Map<Var, Var> varMap;

}

public class UnificationImpl {
    public static final String QUAD_IRI = "http://example.org/quad";

    /**
     *
     * @param aCnf
     * @param bCnf
     */
    public UnificationResult tryUnifyWithEquivalences(Expr a, Expr b) {
        Set<Expr> as = generateEquivalences(a);
        Set<Expr> bs = generateEquivalences(b);

        return null;


    }

    public UnificationResult tryUnify(Expr a, Expr b) {
        if(a.isFunction() && b.isFunction()) {
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
