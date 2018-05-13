package org.aksw.jena_sparql_api.algebra.expr.transform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunctionN;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransformCopy;
import org.apache.jena.sparql.expr.NodeValue;

public class ExprTransformConcatMergeConstants
	extends ExprTransformCopy
{
	protected Predicate<? super Expr> isConcatFunction;

	public ExprTransformConcatMergeConstants(Predicate<? super Expr> isConcatFunction) {
		super();
		this.isConcatFunction = isConcatFunction;
	}

	@Override
	public Expr transform(ExprFunctionN func, ExprList args) {
		Expr result = isConcatFunction.test(func)
				? func.copy(new ExprList(mergeConsecutiveConstants(args.getList())))
				: super.transform(func, args);

		return result;
	}


    /**
     * Merges arguments that are constants together
     * Used for concat: concat("a", "b", "c") becomes concat("abc");
     *
     * @param concat
     * @return
     */
    public static List<Expr> mergeConsecutiveConstants(Iterable<Expr> exprs) {
        String prev = null;
        List<Expr> newExprs = new ArrayList<>();

        for (Expr expr : exprs) {
            if (expr.isConstant()) {
                prev = (prev == null ? "" : prev)
                        + expr.getConstant().asString();
            } else {
                if (prev != null) {
                    newExprs.add(NodeValue.makeString(prev));
                    prev = null;
                }
                newExprs.add(expr);
            }
        }

        if (prev != null) {
            newExprs.add(NodeValue.makeString(prev));
        }

        return newExprs;
    }

}
