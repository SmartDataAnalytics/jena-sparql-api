package org.aksw.jena_sparql_api.algebra.analysis;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.algebra.utils.ExprHolder;
import org.aksw.jena_sparql_api.algebra.utils.OpUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.expr.ExprTransformSubstitute;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;

/**
 *
 * This class captures the following generic SPARQL query set:
 *
 * Select (project / extend) { // postDistinctVarDefs
 *   select distinct (project / extend) { // preDestinctVarDefs
 *      [subOp]
 *      filter(...)
 *   }
 * }
 *
 * - if there is no distinct, there are no post-distinct definitions (indicated by null); only pre-distinct ones
 * - the filters are always expressed to reference pre-distinct variables
 *   this means, that any filter must be expanded first using the post-distinct definitions
 *   and subsequently the pre-distsinct definitions
 * -
 *
 *
 * initially:
 * as long as there is no distinct encountered:
 * - have all projections / expressions affect the pre-distinct definitions
 *
 * When encountering distinct:
 * - Any new distinct makes a prior one obsolete.
 * - The postDistinct expressions become simply identity mappings of the pre-distinct definitions
 *
 *
 *
 *
 *
 * Note: It is possible to have nested distinct's that cannot be flattened, such as:
 *
 * Extend(?foo = <some const>, Distinct(?s ... )

 *
 * @author raven
 *
 */
public class DistinctExtendFilter2 {
    // if there is no distinct, then this map is null
    protected Map<Var, Expr> postDistinctVarDefs;
    protected boolean isDistinct;
    protected Map<Var, Expr> preDistinctVarDefs; // var to defining expression (not for use with aggregate expressions)
    //protected boolean distinct; // whether the set of visible vars according to the varToDef is declared distinct
    protected ExprHolder filter;


    public Op toOp(Op result) {
        if(filter.getExpr().equals(NodeValue.TRUE)) {
            result = OpFilter.filter(filter.getExpr(), result);
        }

        result = OpUtils.applyExtendProject(result, preDistinctVarDefs);

        if(postDistinctVarDefs != null) {
            result = OpDistinct.create(result);
            result = OpUtils.applyExtendProject(result, postDistinctVarDefs);
        }

        return result;
    }

    public DistinctExtendFilter2(Map<Var, Expr> varToDef, boolean distinct, ExprHolder filter) {
        this.preDistinctVarDefs = varToDef;
        //this.distinct = distinct;
        this.filter = filter;
    }

    public static DistinctExtendFilter2 create() {
        DistinctExtendFilter2 result = new DistinctExtendFilter2(null, false, ExprHolder.from(NodeValue.TRUE));
        return result;
    }

    public static Map<Var, Expr> createIdentityMap(Collection<Var> vars) {
        Map<Var, Expr> result = vars.stream()
                .distinct()
                .collect(Collectors.toMap(
                    v -> v,
                    v -> new ExprVar(v),
                    (u, v) -> { throw new RuntimeException("duplicate"); },
                    LinkedHashMap::new
                ));

        return result;
    }

    public static DistinctExtendFilter2 create(Collection<Var> initialVars) {
        Map<Var, Expr> map = createIdentityMap(initialVars);
        DistinctExtendFilter2 result = new DistinctExtendFilter2(map, false, ExprHolder.from(NodeValue.TRUE));
        return result;
    }


    /**
     * Update the projection
     *
     * @param projectVars
     * @return
     */
    public DistinctExtendFilter2 applyProject(Collection<Var> projectVars) {
        Map<Var, Expr> varDefs = isDistinct// postDistinctVarDefs != null
                ? postDistinctVarDefs
                : preDistinctVarDefs;

        if(varDefs == null) {
            if(isDistinct) {
                varDefs = postDistinctVarDefs = new LinkedHashMap<>();
            } else {
                varDefs = preDistinctVarDefs = new LinkedHashMap<>();
            }
            Map<Var, Expr> tmp = createIdentityMap(projectVars);
            varDefs.putAll(tmp);
        }

        // Validate that all requested projected variables are actually present
        boolean isValidRequest = varDefs.keySet().containsAll(projectVars);
        if(!isValidRequest) {
            throw new RuntimeException("Cannot project by non-available vars");
        }

        // If this causes a change in the variables, then distinct becomes false again
        varDefs.keySet().retainAll(projectVars);
//        if(!preDistinctVarDefs.keySet().containsAll(projectVars)) {
//            distinct = false;
//        }

        return this;
    }

//
//    public DistinctExtendFilter applyExtend(Map<Var, Expr> extend) {
//
//        // Expand previously expanded variables
//        Map<Var, Expr> replacements = extend.entrySet().stream()
//            .collect(Collectors.toMap(
//                    Entry::getKey,
//                    e -> {
//                        Expr expr = e.getValue();
//                        Expr r = ExprTransformer.transform(exprTransform, expr);
//                        return r;
//                    }));
//
//        postDistinctVarDefs.putAll(replacements);
//
//        return this;
//    }


    /**
     * Conceptually overrides a prior distinct, and marks
     * all so far visible variables as distinct
     *
     * - If there was no prior distinct:
     *   Initialize the post distinct map to an identity mapping of the visible variables after pre-distinct
     *
     * - Otherwise
     *   Merge the current post-distinct definitions with the pre-distinct ones:
     *   Create a post-distinct identity mapping with all visible variables of pre-distinct
     *
     * @return
     */
    public DistinctExtendFilter2 applyDistinct() {

        // Merge any post distinct var defs with pre distinct
        if(postDistinctVarDefs != null) {
            // Expand all post-distinct vars so far
            preDistinctVarDefs = expandDefs(postDistinctVarDefs, preDistinctVarDefs);
        }

        isDistinct = true;
        postDistinctVarDefs = null;
//        postDistinctVarDefs = new LinkedHashMap<>();
//
//        // Set up the identity mapping for post distinct
//        for(Entry<Var, Expr> e : preDistinctVarDefs.entrySet()) {
//            postDistinctVarDefs.put(e.getKey(), new ExprVar(e.getKey()));
//        }

        return this;
    }

    public DistinctExtendFilter2 applyExtend(VarExprList extend) {
        Map<Var, Expr> map = extend.getExprs();

        applyExtend(map);

        return this;
    }

    public DistinctExtendFilter2 applyExtend(Map<Var, Expr> extend) {

        Map<Var, Expr> tmp = postDistinctVarDefs == null
                ? extend
                : expandDefs(extend, postDistinctVarDefs);

        if(tmp != null) {
            tmp = expandDefs(tmp, preDistinctVarDefs);

            if(postDistinctVarDefs == null) {
                //postDistinctVarDefs = new LinkedHashMap<>();
                preDistinctVarDefs.putAll(tmp);
            } else {
                postDistinctVarDefs.putAll(tmp);
            }
        }

        return this;
    }


    public DistinctExtendFilter2 applyFilter(ExprList contribExprs) {
        for(Expr expr : contribExprs) {
            applyFilter(expr);
        }

        return this;
    }

    /**
     * Expand any filter according to any prior extend
     *
     * @param exprHolder
     * @return
     */
    public DistinctExtendFilter2 applyFilter(Expr contribExpr) {
        // Validation: Make sure all mentioned variables are projected
        Set<Var> mentionedVars = contribExpr.getVarsMentioned();

        Set<Var> availableVars = isDistinct
                ? postDistinctVarDefs == null ? null : postDistinctVarDefs.keySet()
                : preDistinctVarDefs == null ? null : preDistinctVarDefs.keySet()
                ;

       Set<Var> errVars = Sets.difference(mentionedVars, availableVars);
       if(!errVars.isEmpty()) {
           throw new RuntimeException("Reference to unavailable vars: " + errVars + ", available: " + availableVars);
       }


        Expr expandedExpr = expandExpr(contribExpr);

        Expr currentExpr = filter.getExpr();
        Expr expr = NodeValue.TRUE.equals(currentExpr)
                ? expandedExpr
                : new E_LogicalAnd(currentExpr, expandedExpr);

        // perform a logical AND with the given filters
        ExprHolder eh = ExprHolder.from(expr);
        this.filter = eh;

        return this;
    }



    public static String toString(Entry<Var, Expr> e) {
        Var v = e.getKey();
        Expr x = e.getValue();

        String result = x.isVariable() && x.asVar().equals(v)
                ? "" + v
                : "(" + x + " AS " + v + ")";
        return result;
    }


    public static String toString(Map<Var, Expr> varDefs) {
        String result = String.join(" ",
                varDefs.entrySet().stream().map(DistinctExtendFilter2::toString)
                .collect(Collectors.toList()));
        return result;
    }


    @Override
    public String toString() {
        String result = "";

        if(postDistinctVarDefs != null) {
            result += "SELECT " + toString(postDistinctVarDefs) + " {\n";
        }

        result += "SELECT " + (postDistinctVarDefs != null ? "DISTINCT " : "") + toString(preDistinctVarDefs) + " {\n";
        result += "FILTER(" + filter.getExpr() + ")\n";
        //result += ;

        if(postDistinctVarDefs != null) {
            result += "}\n";
        }

        return result;
    }


    /*
     * Utils functions below
     */


    public Expr expandExpr(Expr expr) {
        Expr result;
        // Expand via post-distinct if available
        result = postDistinctVarDefs != null
                ? expandExpr(expr, postDistinctVarDefs)
                : expr;

        result = expandExpr(expr, preDistinctVarDefs);
        return result;
    }



    public static Map<Var, Expr> expandDefs(Map<Var, Expr> src, Map<Var, Expr> varDefs) {
        ExprTransform exprTransform = createExprTransform(varDefs);

        // Expand previously expanded variables
        Map<Var, Expr> result = src.entrySet().stream()
            .collect(Collectors.toMap(
                Entry::getKey,
                e -> {
                    Expr expr = e.getValue();
                    Expr r = ExprTransformer.transform(exprTransform, expr);
                    return r;
                },
                (u, v) -> { throw new RuntimeException("duplicate"); },
                LinkedHashMap::new
            ));

        return result;
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

    public static Expr expandExpr(Expr expr, Map<Var, Expr> varDefs) {
        ExprTransform exprTransform = createExprTransform(varDefs);
        Expr result = ExprTransformer.transform(exprTransform, expr);
        return result;
    }


    public static void main(String[] args) {
        DistinctExtendFilter2 def = DistinctExtendFilter2.create(new LinkedHashSet<>(Arrays.asList(Vars.s, Vars.p, Vars.o)));

        // This extend overrides the definition of ?s
        def.applyExtend(Collections.singletonMap(Vars.s, ExprUtils.parse("?p + ?o")));
        def.applyDistinct();
//        def.applyProject(Arrays.asList(Vars.s));
        def.applyFilter(ExprUtils.parse("?s = ?p"));

        def.applyDistinct();
        def.applyDistinct();
        def.applyDistinct();

        System.out.println(def);
    }

}
