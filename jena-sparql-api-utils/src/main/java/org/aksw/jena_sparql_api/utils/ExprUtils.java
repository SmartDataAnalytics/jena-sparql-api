package org.aksw.jena_sparql_api.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
import org.apache.jena.sparql.expr.FunctionLabel;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ExprTransformNodeElement;


/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 1/8/12
 *         Time: 6:18 PM
 */
public class ExprUtils {

    public static boolean isConstantsOnly(Iterable<Expr> exprs) {
        for(Expr expr : exprs) {
            if(!expr.isConstant()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks wtherer all arguments of the given function are constants (non-recursive).
     *
     * @param fn The function to test
     * @return True if all arguments are constants, false otherwise.
     */
    public static boolean isConstantArgsOnly(ExprFunction fn) {

        if(fn == null) {
            throw new RuntimeException("Null argument should not happen here");
        }

        boolean result = isConstantsOnly(fn.getArgs());

        return result;
    }

    public static String getFunctionId(ExprFunction fn) {

        String result = null;

        result = fn.getOpName();
        if(result != null) {
            return result;
        }



        result = fn.getFunctionIRI();
        if(result != null) {
            return result;
        }


        FunctionLabel label = fn.getFunctionSymbol();
        result = label == null ? null : label.getSymbol();

        /*
        if(result != null) {
            return result;
        }*/

        return result;
    }
    public static <T> int countLeafs(T parent, Function<T, Collection<T>> nodeToChildren) {
        Collection<T> children = nodeToChildren.apply(parent);

        int result = children.isEmpty()
                ? 1
                : children.stream()
                    .mapToInt(c -> countLeafs(c, nodeToChildren)).sum();

        return result;
    }

    public static int countLeafs(Expr expr) {
        int result = countLeafs(expr, ExprUtils::getSubExprs);
        return result;
    }

    /**
     * linearize any structure into a flat list
     *
     * @param op
     * @param stopMarker
     * @param getChildren
     * @return
     */
    public static <T> Stream<T> linearizePrefix(T op, Collection<T> stopMarker, Function<? super T, Iterable<? extends T>> getChildren) {

//        boolean isIdentity = op == stopMarker || (stopMarker != null && stopMarker.equals(op));
        Stream<T> result;
        if(op == null) {
            result = Stream.empty();
        } else {
            Iterable<?extends T> children = getChildren.apply(op);
            Stream<? extends T> x = StreamSupport.stream(children.spliterator(), false);
            //tmp = Stream.concat(x, stopMarker.stream()); // Stream.of(stopMarker)
//            tmp = Stream.concat(tmp, Stream.of(op));

            result =
                Stream.concat(
                    Stream.concat(
                        StreamSupport.stream(children.spliterator(), false).flatMap(e -> linearizePrefix(e, stopMarker, getChildren)),
                        stopMarker.stream()
                    ),
                    Stream.of(op) // Emit parent
                );
        }


        return result;
    }


    /**
     * Traverse the expr
     *
     * @param expr
     * @return
     */
    public static Stream<Expr> linearizePrefix(Expr expr, Collection<Expr> stopMarkers) {
        Stream<Expr> result = linearizePrefix(expr, stopMarkers, ExprUtils::getSubExprs);
        return result;

//        boolean isIdentity = expr == identity || (identity != null && identity.equals(expr));
//        Stream<Expr> tmp;
//        if(isIdentity) {
//            tmp = Stream.empty();
//        } else {
//            List<Expr> children = getSubExprs(expr);
//            tmp = Stream.concat(children.stream(), Stream.of(identity));
//        }
//
//        Stream<Expr> result = Stream.concat(
//                tmp.flatMap(e -> linearizePrefix(e, identity)),
//                Stream.of(expr)); // Emit parent);
//
//        return result;
    }


    public static void main(String[] args) {
        // + ( +(?a ?b) (?c) )
        System.out.println(linearizePrefix(org.apache.jena.sparql.util.ExprUtils.parse("?a + ?b + ?c"), null).collect(Collectors.toList()));
    }

    /**
     * Replace all variable names with the same variable (?a in this case).
     * Useful for checking whether two expressions are structurally equivalent.
     *
     * @param expr
     */
    public static Expr signaturize(Expr expr) {
        NodeTransform nodeTransform = new NodeTransformSignaturize();
        Expr result = NodeTransformLib.transform(nodeTransform, expr);
        return result;
    }

    public static Expr signaturize(Expr expr, Map<? extends Node, ? extends Node> nodeMap) {
        NodeTransform baseTransform = new NodeTransformRenameMap(nodeMap);
        NodeTransform nodeTransform = new NodeTransformSignaturize(baseTransform);
        Expr result = NodeTransformLib.transform(nodeTransform, expr);
        return result;
    }

//    public static Expr applyNodeTransform(Expr expr, Map<? extends Node, ? extends Node> nodeMap) {
//        NodeTransform nodeTransform = new NodeTransformRenameMap(nodeMap);
//        Expr result = NodeTransformLib.transform(nodeTransform, expr);
//        //Expr result = applyNodeTransform(expr, nodeTransform);
//        return result;
//    }

//    public static Expr applyNodeTransform(Expr expr, NodeTransform nodeTransform) {
//        ElementTransform elementTransform = new ElementTransformSubst2(nodeTransform);
//        ExprTransform exprTransform = new ExprTransformNodeElement(nodeTransform, elementTransform);
//
//        Expr result = ExprTransformer.transform(exprTransform, expr);
//        return result;
//    }



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
