package org.aksw.jena_sparql_api.concept_cache.domain;

import java.util.Set;

import org.apache.jena.sparql.expr.Expr;

public class DnfExpr {
    private Expr expr;
    private Set<Set<Expr>> dnf;

    public DnfExpr(Expr expr, Set<Set<Expr>> dnf) {
        super();
        this.expr = expr;
        this.dnf = dnf;
    }
    public Expr getExpr() {
        return expr;
    }
    public Set<Set<Expr>> getDnf() {
        return dnf;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dnf == null) ? 0 : dnf.hashCode());
        result = prime * result + ((expr == null) ? 0 : expr.hashCode());
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
        DnfExpr other = (DnfExpr) obj;
        if (dnf == null) {
            if (other.dnf != null)
                return false;
        } else if (!dnf.equals(other.dnf))
            return false;
        if (expr == null) {
            if (other.expr != null)
                return false;
        } else if (!expr.equals(other.expr))
            return false;
        return true;
    }
    @Override
    public String toString() {
        return "DnfExpr [expr=" + expr + ", dnf=" + dnf + "]";
    }

}