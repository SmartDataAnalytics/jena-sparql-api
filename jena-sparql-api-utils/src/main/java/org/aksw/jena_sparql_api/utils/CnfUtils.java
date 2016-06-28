package org.aksw.jena_sparql_api.utils;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.E_NotEquals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprFunction2;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;


// TODO There is already org.apache.jena.sparql.algebra.optimize.TransformFilterConjunction
public class CnfUtils {

    @SuppressWarnings("unchecked")
    public static <T extends ExprFunction2> T normalize(T expr) {
        Expr a = expr.getArg1();
        Expr b = expr.getArg2();

        Expr result = a.isConstant() && b.isVariable()
            ? expr.copy(b, a)
            : expr
            ;

        return (T)result;
    }

    public static Entry<Var, Node> extractEquality(Collection< ? extends Expr> clause) {
        Entry<Var, Node> result = null;

        if(clause.size() == 1) {
            Expr expr = clause.iterator().next();

            if(expr instanceof E_Equals) {
                E_Equals eq = (E_Equals)expr;

                eq = normalize(eq);

                Expr a = eq.getArg1();
                Expr b = eq.getArg2();

                if(a.isVariable() && b.isConstant()) {
                    Var v = a.asVar();
                    Node c = b.getConstant().getNode();

                    result = new SimpleEntry<>(v, c);
                }
            }
        }

        return result;
    }

    /**
     * Extract from the CNF all mappings from a variable to constant, i.e.
     * if there is ?x = foo, then the result will contain the mapping ?x -> foo.
     *
     *
     * @param cnf
     * @return
     */
    public static Map<Var, Node> getConstants(Iterable<? extends Collection <? extends Expr>> cnf) {
        Map<Var, Node> result = new HashMap<Var, Node>();

        for(Collection<? extends Expr> clause : cnf) {
            Entry<Var, Node> entry = extractEquality(clause);
            if(entry != null) {
                Var v = entry.getKey();
                Node c = entry.getValue();

                Node o = result.get(v);
                if(o != null && !o.equals(c)) {
                    c = NodeValue.FALSE.getNode();
                }

                result.put(v, c);
            }
        }

        return result;
    }

    public static Expr toExpr(Iterable<Set<Expr>> cnf) {
        ExprList exprList = toExprList(cnf);
        Expr result = ExprUtils.andifyBalanced(exprList);
        return result;
    }

    public static ExprList toExprList(Iterable<Set<Expr>> cnf) {
        ExprList result = new ExprList();
        for(Set<Expr> clause : cnf) {
            if(!clause.equals(ClauseUtils.TRUE)) {
                Expr expr = ExprUtils.orifyBalanced(clause);
                result.add(expr);
            }
        }

        return result;
    }


    public static void main(String[] args)
    {
        //!a->b&(a|d&b)

        String sA = "Select * { ?s ?p ?o . Filter((!(!?a) || ?b) && (?a || ?d && ?b)) . }";
        //String sA = "Select * { ?s ?p ?o . Filter(?o != <http://Person>). Optional { { ?x ?y ?z . Filter(?x != <http://x> && ?x = ?y) . } Union { ?x a ?y . Filter(?x = <http://x>) . } } . }";

        //String sA = "Select * { ?s ?p ?o . Filter(!(?s = ?p || ?p = ?o && ?s = ?o || ?o = ?s)) . }";
        //String sA = "Select * { ?s ?p ?o . Filter(!(?s = ?p || ?j = <http://x>)) . }";
        Query qA = QueryFactory.create(sA);

        Op opA = Algebra.compile(qA);
        opA = Algebra.toQuadForm(opA);


        //System.out.println(opA);


        // How to deal with union? variables appearing in them

        //System.out.println(opA.getClass());

        ExprList exprs = FilterUtils.collectExprs(opA, new ExprList());
        Expr expr = ExprUtils.andifyBalanced(exprs);

        Expr x = eval(expr);
        System.out.println("HERE: " + x);

        Set<Set<Expr>> cnf = toSetCnf(expr);
        System.out.println("THERE: " + cnf);
        /*



        ExprList proc = eval(exprs);
        System.out.println(proc);

        List<ExprList> clauses = dnfToClauses(proc);

        System.out.println("Mentioned vars:" + proc.getVarsMentioned());

        System.out.println("Clauses: " + clauses);
*/
    }

    /**
     * Test if the candidate is subsumed by the reference.
     * This means that the candidate must contain at least all constraints of ref
     *
     * @param cand
     * @param ref
     * @return
     */
    public static boolean isSubsumedBy(Set<Set<Expr>> cand, Set<Set<Expr>> ref) {
        boolean result = true;

        for(Set<Expr> clause : ref) {
            boolean isContained = cand.contains(clause);

            if(!isContained) {
                result = false;
                break;
            }
        }

        return result;
    }

    /**
     * Return a sub cnf that where each element of a clause contains all variables
     *
     * @param clauses
     * @param requiredVars
     * @return
     */
    /*
    public static Set<Set<Expr>> filterByVars(Set<Set<Expr>> clauses, Set<Var> requiredVars) {

        Set<Set<Expr>> result = new HashSet<Set<Expr>>();

        for(Set<Expr> clause : clauses) {

            Set<Expr> tmp = null;
            for(Expr expr : clause) {

                if(!expr.getVarsMentioned().containsAll(requiredVars)) {
                    continue;
                }

                if(tmp == null) {
                    tmp = new HashSet<Expr>();
                }

                tmp.add(expr);
            }
            if(tmp != null) {
                result.add(tmp);
            }
        }


        return result;
    }
    */


    public static Set<Set<Expr>> toSetCnf(ExprList exprs)
    {
        List<ExprList> clauses = toClauses(exprs);
        Set<Set<Expr>> cnf = FilterUtils.toSets(clauses);

        return cnf;
    }

    public static Set<Set<Expr>> toSetCnf(Expr expr)
    {
        Set<Set<Expr>> result;
        if(NodeValue.TRUE.equals(expr)) {
            result = new HashSet<>(); // Return a new set, as callers may want to extend the set//Collections.emptySet();
        } else {
            List<ExprList> clauses = toClauses(expr);
            result = FilterUtils.toSets(clauses);
        }

        return result;
    }

    public static List<ExprList> toClauses(Expr expr)
    {
        Expr evaluated = eval(expr);
        return evaluated == null ? null : cnfToClauses(Collections.singleton(evaluated));
    }

    public static List<ExprList> toClauses(ExprList exprs)
    {
        Expr evaluated = eval(ExprUtils.andifyBalanced(exprs));
        return evaluated == null ? null : cnfToClauses(Collections.singleton(evaluated));
    }


    /**
     * This method only words if the input expressions are in DNF,
     * otherwise you will likely get junk back.
     *
     * @param exprs
     * @return
     */
    public static List<ExprList> cnfToClauses(Iterable<Expr> exprs) {
        List<ExprList> result = new ArrayList<ExprList>();

        for(Expr expr : exprs) {
            collectAnd(expr, result);
        }

        return result;
    }


    public static void collectAnd(Expr expr, List<ExprList> list)
    {
        if(expr instanceof E_LogicalAnd) {
            E_LogicalAnd e = (E_LogicalAnd)expr;

            collectAnd(e.getArg1(), list);
            collectAnd(e.getArg2(), list);
        }
        else if(expr instanceof E_LogicalOr) {
            //List<Expr> ors = new ArrayList<Expr>();
            ExprList ors = new ExprList();
            collectOr(expr, ors);

            list.add(ors);
        } else {
            list.add(new ExprList(expr));
        }
    }

    public static void collectOr(Expr expr, ExprList list)
    {
        if(expr instanceof E_LogicalOr) {
            E_LogicalOr e = (E_LogicalOr)expr;

            collectOr(e.getArg1(), list);
            collectOr(e.getArg2(), list);
        } else {
            list.add(expr);
        }
    }


    public static Expr eval(Expr expr)
    {
        if(expr instanceof ExprFunction) {
            return handle((ExprFunction)expr);
        } else {
            return expr;
        }
    }

    public static Expr handle(ExprFunction expr)
    {
        //System.out.println("Converting to KNF: [" + expr.getClass() + "]: " + expr);

        // not(and(A, B)) -> or(not A, not B)
        // not(or(A, B)) -> or(not A, not B)


        if(expr instanceof E_LogicalNot) {

            Expr tmp = ((E_LogicalNot)expr).getArg();
            if (!(tmp instanceof ExprFunction)) {
                return expr;
            }

            ExprFunction child = (ExprFunction)tmp;

            Expr newExpr = expr;

            if (child instanceof E_LogicalAnd) {
                newExpr = new E_LogicalOr(eval(new E_LogicalNot(child.getArg(1))), eval(new E_LogicalNot(child.getArg(2))));
            }
            else if (child instanceof E_LogicalOr) {
                newExpr = new E_LogicalAnd(eval(new E_LogicalNot(child.getArg(1))), eval(new E_LogicalNot(child.getArg(2))));
            }
            else if (child instanceof E_LogicalNot) { // Remove double negation
                newExpr = eval(child.getArg(1));
            }
            else {
                return expr;
            }

            return eval(newExpr);
        }


        else if (expr instanceof E_LogicalAnd) {
            //return expr;
            //return eval(expr);
            return new E_LogicalAnd(eval(expr.getArg(1)), eval(expr.getArg(2)));
        }


        /* Given:
         * (A or B) AND (C x D) becomes:
         * (A and (C x D)) OR (B and (c x D))
         *
         *
         * (A or B) AND (C or D)
         *
         * Goal:
         * (A and C) OR (A and D) OR (B and C) OR (B and D)
         *
         * This method transforms any "or" children of an AND node.
         * other nodes are left untouched:
         * (A or B) AND (c x D) becomes:
         * (A and (c x D)) OR (B and (c x D))
         */
        else if (expr instanceof E_LogicalOr) {

            Expr aa = eval(expr.getArg(1));
            Expr bb = eval(expr.getArg(2));

            E_LogicalAnd a = null;
            Expr b = null;

            if (aa instanceof E_LogicalAnd) {
                a = (E_LogicalAnd)aa;
                b = bb;
            }
            else if(bb instanceof E_LogicalAnd) {
                a = (E_LogicalAnd)bb;
                b = aa;
            }

            if(a == null) {
                return new E_LogicalOr(aa, bb);
            } else {
                return new E_LogicalAnd(eval(new E_LogicalOr(a.getArg(1), b)), eval(new E_LogicalOr(a.getArg(2), b)));
            }
        }

        else if (expr instanceof E_NotEquals) { // Normalize (a != b) to !(a = b) --- this makes it easier to find "a and !a" cases
            return new E_LogicalNot(eval(new E_Equals(expr.getArg(1), expr.getArg(2))));
        }


        return expr;
    }
}
