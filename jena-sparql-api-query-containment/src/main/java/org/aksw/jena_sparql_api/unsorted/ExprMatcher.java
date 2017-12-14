package org.aksw.jena_sparql_api.unsorted;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.combinatorics.algos.CartesianProductUtils;
import org.aksw.combinatorics.collections.Combination;
import org.aksw.commons.collections.CartesianProduct;
import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_StrStartsWith;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;

public class ExprMatcher {

    /**
     * The query expr must subsume the cache expr.
     *
     * @param cache
     * @param query
     * @return
     */
    public static Expr match(Expr cache, Expr query) {
        //System.out.println("MATCHING " + cache + " WITH " + query);
        Expr result = NodeValue.FALSE;

        if(cache.equals(query)) {
            result = NodeValue.TRUE;
        } else {

            // Implementation for dealing with subsumption of strStartsWith expressions
            Entry<E_StrStartsWith, E_StrStartsWith> e = getIfOfSameType(E_StrStartsWith.class, cache, query);
            if(e != null) {
                Entry<Var, NodeValue> pa = ExprUtils.extractVarConstant(e.getKey());
                Entry<Var, NodeValue> pb = ExprUtils.extractVarConstant(e.getKey());

                // TODO Replace by a predicate function
                boolean canMatch = pa.getKey().equals(pb.getKey()) && pa.getValue().isString() && pb.getValue().isString();
                if(canMatch) {
                    String sa = pa.getValue().asString();
                    String sb = pa.getValue().asString();

                    if(sb.startsWith(sa)) {
                        result = e.getValue();
                    }
                }


            }
        }

        return result;
    }

    public static <T> Entry<T, T> getIfOfSameType(Class<T> clazz, Expr a, Expr b) {
        @SuppressWarnings("unchecked")
        Entry<T, T> result = clazz.isAssignableFrom(a.getClass()) && clazz.isAssignableFrom(b.getClass())
            ? new SimpleEntry<T, T>((T)a, (T)b)
            : null;

        return result;
    }


    public static CartesianProduct<Combination<Expr, Expr, Expr>> match(Set<Expr> cacheConjunction, Set<Expr> queryConjunction) {
        CartesianProduct<Combination<Expr, Expr, Expr>> result = CartesianProductUtils.createOnDemandCartesianProduct(
                cacheConjunction,
                queryConjunction,
                ExprMatcher::match,
                x -> x.equals(NodeValue.FALSE));

        return result;
    }

    //public static

//    public static Collection<Expr> addContribution(Collection<Expr> clause, Expr expr) {
//        Expr result;
//        if(expr.equals(NodeValue.FALSE)) {
//            result = NodeValue.FALSE;
//        } else if(expr.equals(NodeValue.TRUE)) {
//            result = clause;
//        }
//    }

}
