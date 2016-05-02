package org.aksw.jena_sparql_api.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collections.IterableCollection;
import org.aksw.commons.util.Pair;
import org.aksw.commons.util.factory.Factory2;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ExprTransformNodeElement;


/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 1/8/12
 *         Time: 6:18 PM
 */
public class ExprUtils {



    /**
     * Traverse the expe
     *
     * @param expr
     * @return
     */
    public static Stream<Expr> prefix(Expr expr, Expr identity) {
        boolean isIdentity = expr == identity || (identity != null && identity.equals(expr));
        Stream<Expr> tmp;
        if(isIdentity) {
            tmp = Stream.empty();
        } else {
            List<Expr> children = getSubExprs(expr);
            tmp = Stream.concat(children.stream(), Stream.of(identity));
        }

        Stream<Expr> result = Stream.concat(
                Stream.of(expr), // Emit parent
                tmp.flatMap(e -> prefix(e, identity)));

        return result;
    }


    public static void main(String[] args) {
        // + ( +(?a ?b) (?c) )
        System.out.println(prefix(org.apache.jena.sparql.util.ExprUtils.parse("?a + ?b + ?c"), null).collect(Collectors.toList()));
    }

    /**
     * Replace all variable names with the same variable (?a in this case).
     * Useful for checking whether two expressions are structurally equivalent.
     *
     * @param expr
     */
    public static Expr canonicalize(Expr expr) {
        NodeTransform nodeTransform = (node) -> node.isVariable() ? Vars.a : node;
        Expr result = transform(expr, nodeTransform);
        return result;
    }

    public static Expr transform(Expr expr, Map<? extends Node, ? extends Node> nodeMap) {
        NodeTransform nodeTransform = new NodeTransformRenameMap(nodeMap);
        Expr result = transform(expr, nodeTransform);
        return result;
    }

    public static Expr transform(Expr expr, NodeTransform nodeTransform) {
        ElementTransform elementTransform = new ElementTransformSubst2(nodeTransform);
        ExprTransform exprTransform = new ExprTransformNodeElement(nodeTransform, elementTransform);

        Expr result = ExprTransformer.transform(exprTransform, expr);
        return result;
    }



    public static Expr andifyBalanced(Expr ... exprs) {
        return andifyBalanced(Arrays.asList(exprs));
    }

    public static Expr orifyBalanced(Expr... exprs) {
        return orifyBalanced(Arrays.asList(exprs));
    }

    public static List<String> extractNames(Collection<Var> vars) {
        List<String> result = new ArrayList<String>();
        for(Var var : vars) {
            result.add(var.getName());
        }

        return result;
    }

    public static Expr andifyBalanced(Iterable<Expr> exprs) {
        return opifyBalanced(exprs, new Factory2<Expr>() {
            @Override
            public Expr create(Expr a, Expr b)
            {
                return new E_LogicalAnd(a, b);
            }
        });
    }

    /**
     * Concatenates the sub exressions using a binary operator
     *
     * and(and(0, 1), and(2, 3))
     *
     * @param exprs
     * @return
     */
    public static <T> T opifyBalanced(Iterable<T> exprs, Factory2<T> exprFactory) {
        if(exprs.iterator().hasNext() == false) { //isEmpty()) {
            return null;
        }

        List<T> current = new ArrayList<T>(IterableCollection.wrap(exprs));

        while(current.size() > 1) {

            List<T> next = new ArrayList<T>();
            T left = null;
            for(T expr : current) {
                if(left == null) {
                    left = expr;
                } else {
                    T newExpr = exprFactory.create(left, expr);
                    next.add(newExpr);
                    left = null;
                }
            }

            if(left != null) {
                next.add(left);
            }

            current.clear();

            List<T> tmp = current;
            current = next;
            next = tmp;
        }

        return current.get(0);
    }

    public static Expr orifyBalanced(Iterable<Expr> exprs) {
        return opifyBalanced(exprs, new Factory2<Expr>() {
            @Override
            public Expr create(Expr a, Expr b)
            {
                return new E_LogicalOr(a, b);
            }
        });
    }




    public static Pair<Var, NodeValue> extractConstantConstraint(Expr expr) {
        if(expr instanceof E_Equals) {
            E_Equals e = (E_Equals)expr;
            return extractConstantConstraint(e.getArg1(), e.getArg2());
        }

        return null;
    }

    public static Pair<Var, NodeValue> extractConstantConstraint(Expr a, Expr b) {
        Pair<Var, NodeValue> result = extractConstantConstraintDirected(a, b);
        if(result == null) {
            result = extractConstantConstraintDirected(b, a);
        }

        return result;
    }

    /*
    public static void extractConstantConstraints(Expr a, Expr b, EquiMap<Var, NodeValue> equiMap) {
        extractConstantConstraints(a, b, equiMap.getKeyToValue());
    }*/


    /**
     * If a is a variable and b is a constant, then a mapping of the variable to the
     * constant is put into the map, and true is returned.
     * Otherwise, nothing is changed, and false is returned.
     *
     * A mapping of a variable is set to null, if it is mapped to multiple constants
     *
     *
     * @param a
     * @param b
     * @return
     */
    public static Pair<Var, NodeValue> extractConstantConstraintDirected(Expr a, Expr b) {
        if(!(a.isVariable() && b.isConstant())) {
            return null;
        }

        Var var = a.getExprVar().asVar();
        NodeValue nodeValue = b.getConstant();

        return Pair.create(var, nodeValue);
    }


    public static List<Expr> getSubExprs(Expr expr) {
        List<Expr> result = expr != null && expr.isFunction()
                ? expr.getFunction().getArgs()
                : Collections.emptyList()
                ;

        return result;
    }

    @Deprecated
    public static Collection<? extends Expr> getSubExpressions(Expr expr, boolean reflexive) {
        Set<Expr> result = new HashSet<Expr>();

        if(reflexive) {
            result.add(expr);
        }

        getSubExpressions(expr, result);

        return result;
    }

    @Deprecated
    public static void getSubExpressions(Expr expr, Set<Expr> result) {
        if(expr.isFunction()) {
            ExprFunction f = (ExprFunction)expr;

            for(int i = 1; i <= f.numArgs(); ++i) {
                Expr arg = f.getArg(i);
                if(!result.contains(arg)) {
                    result.add(arg);
                    getSubExpressions(arg, result);
                }
            }
        }

    }

    /*
    public static boolean extractConstantConstraintsDirected(Expr a, Expr b, EquiMap<Var, NodeValue> equiMap) {
        return extractConstantConstraintsDirected(a, b, equiMap.getKeyToValue());
    }*/

}
