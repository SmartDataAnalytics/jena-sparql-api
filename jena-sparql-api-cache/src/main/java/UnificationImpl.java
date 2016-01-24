import java.util.HashSet;
import java.util.Set;

import org.aksw.jena_sparql_api.utils.ExprListUtils;
import org.aksw.jena_sparql_api.utils.QuadUtils;

import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_Function;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;

public class UnificationImpl {
    public static final String QUAD_IRI = "http://example.org/quad";

    public void unify(Set<Set<Expr>> aCnf, Set<Set<Expr>> bCnf) {

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
