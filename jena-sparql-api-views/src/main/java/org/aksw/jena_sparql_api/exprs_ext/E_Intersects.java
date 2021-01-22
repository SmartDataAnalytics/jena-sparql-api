package org.aksw.jena_sparql_api.exprs_ext;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction2;
import org.apache.jena.sparql.expr.NodeValue;

public class E_Intersects
	extends ExprFunction2
{
    private static final String symbol = "ST_Intersects" ;

	
	public E_Intersects(Expr a, Expr b) {
		super(a, b, symbol);
	}

	@Override
	public NodeValue eval(NodeValue x, NodeValue y) {
		throw new UnsupportedOperationException();
	}


	@Override
	public Expr copy(Expr arg1, Expr arg2) {
		return new E_Intersects(arg1, arg2);
	}
}
