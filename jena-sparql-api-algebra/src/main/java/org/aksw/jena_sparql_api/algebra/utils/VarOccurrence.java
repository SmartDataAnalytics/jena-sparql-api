package org.aksw.jena_sparql_api.algebra.utils;

import java.util.Set;

import org.apache.jena.sparql.expr.Expr;

/**
 *
 * @author raven
 *
 */
public class VarOccurrence
{
    private Set<Set<Expr>> quadCnf;
    private int component;

    public VarOccurrence(Set<Set<Expr>> quadCnf, int component) {
        this.quadCnf = quadCnf;
        this.component = component;
    }

    public Set<Set<Expr>> getQuadCnf() {
        return quadCnf;
    }


    public int getComponent() {
        return component;
    }

    @Override
    public String toString() {
        return "VarOccurrence [quadCnf=" + quadCnf + ", component="
                + component + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + component;
        result = prime * result
                + ((quadCnf == null) ? 0 : quadCnf.hashCode());
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
        VarOccurrence other = (VarOccurrence) obj;
        if (component != other.component)
            return false;
        if (quadCnf == null) {
            if (other.quadCnf != null)
                return false;
        } else if (!quadCnf.equals(other.quadCnf))
            return false;
        return true;
    }


}