package org.aksw.jena_sparql_api.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.optimize.ExprTransformConstantFold;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.BindingRoot;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.NodeTransform;

public class ClauseUtils
{

    public static final Set<Expr> TRUE = Collections.<Expr>singleton(NodeValue.TRUE);
    public static final Set<Expr> FALSE = Collections.<Expr>singleton(NodeValue.FALSE);


    public static Map<Var, NodeValue> extractConstantConstraints(Collection<? extends Expr> clause) {
    	Map<Var, NodeValue> result = new HashMap<>(clause.size());
    	for(Expr expr : clause) {
    		Entry<Var, NodeValue> e = ExprUtils.extractConstantConstraint(expr);
    		if(e != null) {
    			result.put(e.getKey(), e.getValue());
    		}
    	}
    	return result;
    }

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

    //Iterable<? extends Iterable<? extends Expr>
    public static Set<Expr> signaturize(Iterable<? extends Expr> clause) {
        Set<Expr> result = StreamSupport.stream(clause.spliterator(), false)
            .map(e -> ExprUtils.signaturize(e))
            .collect(Collectors.toSet());

        return result;
    }


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
        Expr exprCopy = expr.copySubstitute(BindingRoot.create());
        Expr folded = ExprTransformer.transform(new ExprTransformConstantFold(), exprCopy) ;

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
