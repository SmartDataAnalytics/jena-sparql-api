package org.aksw.jena_sparql_api.algebra.expr.transform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprFunctionN;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransformCopy;

/**
 * Flatten nested functions, such as f(a, f(b, c), d) -> f(a, b, c, d)
 * Useful for concat expressions
 * 
 * 
 * @author raven Mar 29, 2018
 *
 */
public class ExprTransformFlattenFunction
	extends ExprTransformCopy
{
	protected Predicate<? super Expr> isFlattenableFunction;

	public ExprTransformFlattenFunction(Predicate<? super Expr> isFlattenableFunction) {
		super();
		this.isFlattenableFunction = isFlattenableFunction;
	}
	
	@Override
	public Expr transform(ExprFunctionN func, ExprList args) {

		Expr result = isFlattenableFunction.test(func)
			? func.copy(new ExprList(flatten(func.getArgs(), isFlattenableFunction)))
			: super.transform(func, args)
			;

		return result;
	}

	public static List<Expr> flatten(List<Expr> args, Predicate<? super Expr> isFlattenableFunction) {
		List<Expr> tmpArgs = new ArrayList<>();

		boolean change = false;
		for(Expr arg : args) {
			if(arg.isFunction()) {
				ExprFunction fn = arg.getFunction();
				if(isFlattenableFunction.test(fn)) {
					change = true;
					tmpArgs.addAll(fn.getArgs());
				} else {
					tmpArgs.add(arg);
				}
			} else {
				tmpArgs.add(arg);
			}
		}

		List<Expr> result = change ? tmpArgs : args;
		
		return result;
	}
}
