package org.aksw.jena_sparql_api.shape;

import com.hp.hpl.jena.sparql.expr.Expr;

/**
 * Combine an expression with a direction
 * @author raven
 *
 */
public class OpOutgoing {
    private final Expr expr;
    private final boolean isInverse;
    
    public OpOutgoing(Expr expr, boolean isInverse) {
        super();
        this.expr = expr;
        this.isInverse = isInverse;
    }

    public Expr getExpr() {
        return expr;
    }

    public boolean isInverse() {
        return isInverse;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((expr == null) ? 0 : expr.hashCode());
        result = prime * result + (isInverse ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OpOutgoing other = (OpOutgoing) obj;
        if (expr == null) {
            if (other.expr != null)
                return false;
        } else if (!expr.equals(other.expr))
            return false;
        if (isInverse != other.isInverse)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ExprDir [expr=" + expr + ", isInverse=" + isInverse + "]";
    }
}