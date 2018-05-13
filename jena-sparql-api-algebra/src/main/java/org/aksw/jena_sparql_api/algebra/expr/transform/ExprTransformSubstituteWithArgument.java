package org.aksw.jena_sparql_api.algebra.expr.transform;

import java.util.function.Predicate;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction1;
import org.apache.jena.sparql.expr.ExprTransformCopy;

/**
 * Substitute unary function with its argument - i.e. f(x) -> x
 * Used for example in the r2rml exporter, where 
 * explicit string conversions using the 'STR' functions are removed in order to build to
 * r2rml uri template strings:
 * 
 * concat('http://foo.bar/baz/', str(?intColumn)) -> "http://foo.bar/baz/{intColumn}"
 * 
 * @author raven Mar 30, 2018
 *
 */
public class ExprTransformSubstituteWithArgument
	extends ExprTransformCopy
{
	protected Predicate<? super Expr> isSubstitutionCanditate;

	public ExprTransformSubstituteWithArgument(Predicate<? super Expr> isSubstitutionCanditate) {
		super();
		this.isSubstitutionCanditate = isSubstitutionCanditate;
	}

	@Override
	public Expr transform(ExprFunction1 func, Expr arg) {
		Expr result = isSubstitutionCanditate.test(func)
				? arg
				: super.transform(func, arg);
	
		return result;
	}
}
