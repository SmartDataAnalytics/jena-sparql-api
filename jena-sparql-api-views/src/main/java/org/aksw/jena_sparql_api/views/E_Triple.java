package org.aksw.jena_sparql_api.views;

import java.util.List;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunctionN;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;

public class E_Triple
    extends ExprFunctionN
{
    public E_Triple(Expr s, Expr p, Expr o) {
        super(E_Triple.class.getName(), s, p, o);
    }

    public Expr getSubjectExpr() {
        return args.get(0);
    }

//    public Expr setSubjectExpr(E_RdfTerm expr) {
//        args.set(0, expr);
//        return this;
//    }

    public Expr getPredicateExpr() {
        return args.get(1);
    }

//    public E_Triple setPredicateExpr(E_RdfTerm expr) {
//        components.set(1, expr);
//        return this;
//    }

    public Expr getObjectExpr() {
        return args.get(2);
    }

//    public E_Triple setObjectExpr(E_RdfTerm expr) {
//        components.set(2, expr);
//        return this;
//    }


    @Override
    public NodeValue eval(List<NodeValue> args) {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public E_Triple copy(ExprList newArgs) {
        throw new RuntimeException("not implemented yet");
    }

}
