package org.aksw.jena_sparql_api.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.ext.com.google.common.collect.HashMultimap;
import org.apache.jena.ext.com.google.common.collect.SetMultimap;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.expr.ExprTransformSubstitute;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.ExprVars;
import org.apache.jena.sparql.graph.NodeTransform;

public class VarExprListUtils {

    /**
     * Invert the mapping of a var expr list and return it as a jena-shaded guava Multimap.
     * ExprVars are generated for each variable that is otherwise mapped to null.
     *
     * @param vel
     * @return
     */
    public static SetMultimap<Expr, Var> invert(VarExprList vel) {
        // Map exprs to variables derived from the projection
        SetMultimap<Expr, Var> result = HashMultimap.create();
        for(Var v : vel.getVars()) {
            Expr expr = vel.getExpr(v);
            Expr effectiveExpr = expr == null ? new ExprVar(v) : expr;
            result.put(effectiveExpr, v);
        }

        return result;
    }

    /**
     * Create a map of the variables that are defined in terms of a constant
     *
     * Can be combined with applyExprTransform to e.g. fold constants first.
     *
     * @param vel
     * @return
     */
    public static Map<Var, Node> extractConstants(VarExprList vel) {
        Map<Var, Node> result = new LinkedHashMap<>();
        for(Var var : vel.getVars()) {
            Expr expr = vel.getExpr(var);

            if(expr.isConstant()) {
                Node node = expr.getConstant().asNode();
                result.put(var, node);
            }
        }

        return result;
    }

    public static boolean hasExprs(VarExprList vel) {
        VarExprList copy = VarExprListUtils.createFromMap(vel.getExprs());
        boolean result = !copy.getExprs().isEmpty();
        return result;
    }


    /**
     * In place canonicalization that removes identity mappings of variables to themselves
     *
     * @param vel
     * @return
     */
    public static boolean canonicalize(VarExprList vel) {
        boolean result = false;
        Map<Var, Expr> map = vel.getExprs();
        Iterator<Entry<Var, Expr>> it = map.entrySet().iterator();
        while(it.hasNext()) {
            Entry<Var, Expr> e = it.next();
            Var v = e.getKey();
            Expr x = e.getValue();

            if(ExprUtils.isSame(v, x)) {
                it.remove();
                result = true;
            }
        }

        return result;
    }

    public static VarExprList add(VarExprList result, Var v, Expr e) {
        if(e == null || ExprUtils.isSame(v, e)) {
            result.add(v);
        } else {
            result.add(v, e);
        }

        return result;
    }

    public static VarExprList add(VarExprList result, Var v, Var w) {
        if(v.equals(w)) {
            result.add(v);
        } else {
            result.add(v, new ExprVar(w));
        }

        return result;
    }


    public static VarExprList createFromMap(Map<Var, Expr> map) {
        VarExprList result = new VarExprList();
        for(Entry<Var, Expr> e : map.entrySet()) {
            Var v = e.getKey();
            Expr w = e.getValue();

            add(result, v, w);
        }

        return result;
    }

    /**
     * Create a projection to rename variables from key to value.
     *
     * Note that given two variables ?s and ?o,
     * for mapping ?s to ?o the projection is (?s AS ?o) and thus
     * the invocation on the VarExprList is vel.add(?o, ?s), which can be read as
     * ?o is defined by ?s
     *
     */
    public static VarExprList createFromVarMap(Map<Var, Var> varMap) {
        VarExprList result = new VarExprList();
        for(Entry<Var, Var> e : varMap.entrySet()) {
            Var v = e.getKey();
            Var w = e.getValue();

            add(result, w, v);
        }

        return result;
    }


    public static Set<Var> getDefinedVars(VarExprList vel) {
        Set<Var> result = definedVars(new LinkedHashSet<>(), vel);
        return result;
    }

    public static <T extends Collection<Var>> T definedVars(T acc, VarExprList vel) {
        acc.addAll(vel.getVars());
        return acc;
    }

    public static Set<Var> getVarsMentionedInBody(VarExprList vel) {
        Set<Var> result = varsMentionedInBody(new LinkedHashSet<>(), vel);
        return result;
    }

    public static <T extends Collection<Var>> T varsMentionedInBody(T acc, VarExprList vel) {
        for(Entry<Var, Expr> entry : vel.getExprs().entrySet()) {
            Expr e = entry.getValue();
            if(e != null) {
                ExprVars.varsMentioned(acc, e);
            }
        }
        return acc;
    }

    public static Set<Var> getVarsMentioned(VarExprList vel) {
        Set<Var> result = varsMentioned(new LinkedHashSet<>(), vel);
        return result;
    }

    public static <T extends Collection<Var>> T varsMentioned(T acc, VarExprList vel) {
        definedVars(acc, vel);
        varsMentionedInBody(acc, vel);
        return acc;
    }

    @Deprecated // This method has unclear semantics ; use the getVarsMentioned variants instead
    public static Set<Var> getRefVars(VarExprList vel) {
        Set<Var> result = new HashSet<Var>();

        for(Entry<Var, Expr> entry : vel.getExprs().entrySet()) {
            if(entry.getValue() == null) {
                result.add(entry.getKey());
            } else {
                Set<Var> vs = ExprVars.getVarsMentioned(entry.getValue());
                result.addAll(vs);
            }
        }
        return result;
    }

    private static Expr transform(Expr expr, ExprTransform exprTransform)
    {
        if ( expr == null || exprTransform == null )
            return expr ;
        return ExprTransformer.transform(exprTransform, expr) ;
    }

    public static Map<Var, Expr> applyNodeTransform(Map<Var, Expr> varExpr, NodeTransform nodeTransform)
    {
        Map<Var, Expr> result = varExpr.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> (Var)nodeTransform.apply(e.getKey()),
                        e -> e.getValue().applyNodeTransform(nodeTransform),
                        (u, v) -> { throw new RuntimeException("Duplicate key"); },
                        LinkedHashMap::new
                ));

        return result;
    }


    // Copied from package org.apache.jena.sparql.algebra.ApplyTransformVisitor;
    public static VarExprList transform(VarExprList varExpr, ExprTransform exprTransform)
    {
        List<Var> vars = varExpr.getVars() ;
        VarExprList varExpr2 = new VarExprList() ;
        boolean changed = false ;
        for ( Var v : vars )
        {
            Expr newVE = exprTransform.transform(new ExprVar(v));
            Var newV = newVE == null ? v : ((ExprVar)newVE).asVar();

            // Once changed is true, it stays true
            changed = changed || !v.equals(newV);

            Expr e = varExpr.getExpr(v) ;
            Expr e2 =  e ;
            if ( e != null )
                e2 = transform(e, exprTransform) ;
            if ( e2 == null )
                varExpr2.add(newV) ;
            else
                varExpr2.add(newV, e2) ;
            if ( e != e2 )
                changed = true ;
        }
        if ( ! changed )
            return varExpr ;
        return varExpr2 ;
    }


    public static ExprTransform createExprTransform(Map<Var, Expr> varDefs) {
        // TODO Avoid creating the copy of the map
        Map<String, Expr> tmp = varDefs.entrySet().stream()
                .collect(Collectors.toMap(
                    e -> e.getKey().getName(),
                    Entry::getValue,
                    (u, v) -> { throw new RuntimeException("duplicate"); },
                    LinkedHashMap::new
                ));

        ExprTransform result = new ExprTransformSubstitute(tmp);
        return result;
    }


    public static void replace(VarExprList dst, VarExprList src) {
        if(dst != src) {
            dst.clear();
            copy(dst, src);
        }
    }

    public static VarExprList copy(VarExprList dst, VarExprList src) {
        for(Var v : src.getVars()) {
            Expr e = src.getExpr(v);
            if(e == null) {
                dst.add(v);
            } else {
                dst.add(v, e);
            }
        }

        return dst;
    }
}
