package org.aksw.jena_sparql_api.algebra.transform;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.apache.jena.sparql.expr.E_Add;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_Multiply;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction2;
import org.apache.jena.sparql.expr.ExprTransformCopy;

public class ExprTransformVariableOrder
	extends ExprTransformCopy
{
	protected Predicate<Expr> isCommutative;

	public ExprTransformVariableOrder() {
		this(ExprTransformVariableOrder::isCommutative);
	}

	public ExprTransformVariableOrder(Predicate<Expr> isCommutative) {
		super();
		this.isCommutative = isCommutative;
	}

	@Override
	public Expr transform(ExprFunction2 func, Expr a, Expr b) {
		List<Expr> args = Arrays.asList(a, b);
		if(isCommutative.test(func)) {
			Collections.sort(args, ExprUtils::compare);
		}

		return super.transform(func, args.get(0), args.get(1));
	}

	public static final Set<Class<?>> symmetricExprClasses = new HashSet<>(Arrays.asList(
			E_Equals.class,
			E_Add.class,
			E_Multiply.class
			));

	public static boolean isCommutative(Expr e) {
		boolean result = symmetricExprClasses.contains(e.getClass());
		return result;
	}
}
