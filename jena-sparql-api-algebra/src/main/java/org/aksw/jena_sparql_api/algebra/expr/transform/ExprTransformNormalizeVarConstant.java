package org.aksw.jena_sparql_api.algebra.expr.transform;

import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction2;
import org.apache.jena.sparql.expr.ExprTransformCopy;

public class ExprTransformNormalizeVarConstant
	extends ExprTransformCopy
{
	@Override
	public Expr transform(ExprFunction2 func, Expr a, Expr b) {

        Expr result = func instanceof E_Equals && a.isConstant() && b.isVariable()
                ? super.transform(func, b, a)
                : super.transform(func, a, b)
                ;

        return result;
	}
}
