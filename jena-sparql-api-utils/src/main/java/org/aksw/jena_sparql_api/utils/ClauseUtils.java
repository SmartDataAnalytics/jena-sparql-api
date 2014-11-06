package org.aksw.jena_sparql_api.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot;
import com.hp.hpl.jena.sparql.expr.E_LogicalNot;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.graph.NodeTransform;

public class ClauseUtils
{

    public static final Set<Expr> TRUE = Collections.<Expr>singleton(NodeValue.TRUE);

    /*
     * Use ExprIndex.filterByVars instead
    public static Set<Clause> filterByVars(Collection<Clause> clauses, Set<Var> requiredVars) {

        Set<Clause> result = new HashSet<Clause>();

        for(Clause clause : clauses) {
            Set<Var> clauseVars = clause.getVarsMentioned();

            if(!clauseVars.containsAll(requiredVars)) {
                continue;
            }

            result.add(clause);
        }


        return result;
    }*/


    public static Set<Set<Expr>> filterByVars(Set<Set<Expr>> clauses, Set<Var> requiredVars) {

        Set<Set<Expr>> result = new HashSet<Set<Expr>>();

        for(Set<Expr> clause : clauses) {
            Set<Var> clauseVars = ClauseUtils.getVarsMentioned(clause);

            if(!clauseVars.containsAll(requiredVars)) {
                continue;
            }

            result.add(clause);
        }


        return result;
    }

    /**
     * false means that it is no satisfiable.
     *
     * true is actually a 'maybe'
     *
     * @param clause
     * @return
     */
    public static boolean isSatisfiable(Set<Expr> clause)
    {
        for(Expr expr : clause) {
            if(expr.equals(NodeValue.FALSE)) {
                return false;
            }

            if(!isSatisfiable(expr)) {
                return false;
            }

            if(expr instanceof E_LogicalNot) {
                Expr child = ((E_LogicalNot)expr).getArg();

                if(clause.contains(child)) {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean isSatisfiable(Expr expr)
    {
        // NOTE Folding does not detect cases such as E_LogicalAnd(E_Equals(x = y), false)
        Expr folded = expr.copySubstitute(BindingRoot.create(), true);
        return !folded.equals(NodeValue.FALSE);
    }

    public static Set<Var> getVarsMentioned(Iterable<? extends Expr> clause)
    {
        Set<Var> result = new HashSet<Var>();

        for(Expr expr : clause) {
            result.addAll(expr.getVarsMentioned());
        }

        return result;
    }

    public static Set<Set<Expr>> applyNodeTransformSet(Set<Set<Expr>> clauses, NodeTransform nodeTransform) {
        Set<Set<Expr>> result = new HashSet<Set<Expr>>();
        for(Set<Expr> clause : clauses) {
            Set<Expr> transformedClause = applyNodeTransform(clause, nodeTransform);
            result.add(transformedClause);
        }

        return result;
    }

    public static Set<Expr> applyNodeTransform(Set<Expr> clause, NodeTransform nodeTransform) {
        Set<Expr> result = new HashSet<Expr>();
        for(Expr expr : clause) {
            Expr transformedExpr = expr.applyNodeTransform(nodeTransform);
            result.add(transformedExpr);
        }

        return result;
    }

    /*
    public static Set<Var> getVarsMentioned(Set<Expr> clause)
    {
        Set<Var> vars = new HashSet<Var>();

        for(Expr expr : clause) {
            Set<Var> exprVars = expr.getVarsMentioned();

            if(vars.isEmpty()) { // this happens on the first expr
                vars.addAll(exprVars);
            } else {
                vars.retainAll(exprVars);
                if(vars.isEmpty()) {
                    break;
                }
            }
        }

        return vars;
    }
     */

}
