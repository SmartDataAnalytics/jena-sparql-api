package org.aksw.jena_sparql_api.relation;

import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;


public class TripleConstraintImpl
    implements TripleConstraint
{
    protected Triple pattern;
    protected Expr expr;

    public TripleConstraintImpl(Triple pattern, Expr expr) {
        super();
        this.pattern = pattern;
        this.expr = expr;
    }

    public static TripleConstraint create(Expr expr) {
        return create(Triple.createMatch(null, null, null), expr);
    }

    public static TripleConstraint create(Node s, Node p, Node o) {
        return create(Triple.createMatch(s, p, o));
    }

    public static TripleConstraint create(Triple pattern) {
        return create(pattern, null);
    }

    public static TripleConstraint create(Triple triple, Expr expr) {
        return new TripleConstraintImpl(triple, expr);
    }

    @Override
    public boolean test(Triple t) {
        boolean result;
        Binding b = TripleUtils.tripleToBinding(pattern, t);
        if (b == null) {
            result = false;
        } else {
            if (expr == null) {
                result = true;
            } else {
                NodeValue nv = ExprUtils.eval(expr, b);
                result = nv.getBoolean();
            }
        }
        return result;
    }

    @Override
    public Triple getMatchTriple() {
        return pattern;
    }

    @Override
    public boolean isMatchTripleExhaustive() {
        return expr == null;
    }

    @Override
    public Expr getExpr() {
        return expr;
    }
}