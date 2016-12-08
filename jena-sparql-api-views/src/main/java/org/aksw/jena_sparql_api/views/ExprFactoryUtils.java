package org.aksw.jena_sparql_api.views;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BinaryOperator;

import org.apache.jena.sparql.expr.E_GreaterThan;
import org.apache.jena.sparql.expr.E_GreaterThanOrEqual;
import org.apache.jena.sparql.expr.E_LessThan;
import org.apache.jena.sparql.expr.E_LessThanOrEqual;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction2;
import org.apache.jena.sparql.sse.Tags;

public class ExprFactoryUtils {

    private static final Map<String, BinaryOperator<Expr>> binaryFactories = new HashMap<String, BinaryOperator<Expr>>();

    public static final BinaryOperator<Expr> factoryLogicalAnd = (a, b) -> new E_LogicalAnd(a, b);
    public static final BinaryOperator<Expr> factoryLogicalOr = (a, b) -> new E_LogicalOr(a, b);

    public static final BinaryOperator<Expr> factoryLessThan = (a, b) -> new E_LessThan(a, b);
    public static final BinaryOperator<Expr> factoryLessThanOrEqual = (a, b) -> new E_LessThanOrEqual(a, b);
    public static final BinaryOperator<Expr> factoryGreaterThanOrEqual = (a, b) -> new E_GreaterThanOrEqual(a, b);
    public static final BinaryOperator<Expr> factoryGreaterThan = (a, b) -> new E_GreaterThan(a, b);

    static {
        binaryFactories.put(Tags.symLT, factoryLessThan);
        binaryFactories.put(Tags.symLE, factoryLessThanOrEqual);
        binaryFactories.put(Tags.symGE, factoryGreaterThanOrEqual);
        binaryFactories.put(Tags.symGT, factoryGreaterThan);
    }

    public static BinaryOperator<Expr> getFactory2(String tag) {
        BinaryOperator<Expr> result = binaryFactories.get(tag);
        return result;
    }

    public static BinaryOperator<Expr> createCopyFactory2(ExprFunction2 prototype) {
        BinaryOperator<Expr> result = (a, b) ->  ExprCopy.getInstance().copy(prototype, a, b);

        return result;
    }
}
