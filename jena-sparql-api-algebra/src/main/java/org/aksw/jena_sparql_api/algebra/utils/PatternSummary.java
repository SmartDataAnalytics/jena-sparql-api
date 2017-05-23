package org.aksw.jena_sparql_api.algebra.utils;

import java.util.Set;

import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;



public class PatternSummary
{
    private QuadFilterPattern originalPattern;
    private QuadFilterPatternCanonical canonicalPattern;


    // The pattern's filters in cnf
    //private Set<Set<Expr>> filterCnf;

    // The constraints on the quad pattern derived from the original pattern
    //private Set<Set<Set<Expr>>> quadCnfs;

    private IBiSetMultimap<Quad, Set<Set<Expr>>> quadToCnf;

    // Variable distributions among the quads
    private IBiSetMultimap<Var, VarOccurrence> varOccurrences;

    public PatternSummary(QuadFilterPattern originalPattern,
            QuadFilterPatternCanonical canonicalPattern,
            //Set<Set<Set<Expr>>> quadCnfs,
            IBiSetMultimap<Quad, Set<Set<Expr>>> quadToCnf,
            IBiSetMultimap<Var, VarOccurrence> varOccurrences) {
        super();
        this.originalPattern = originalPattern;
        this.canonicalPattern = canonicalPattern;
        //this.quadCnfs = quadCnfs;
        this.quadToCnf = quadToCnf;
        this.varOccurrences = varOccurrences;
    }

    public QuadFilterPattern getOriginalPattern() {
        return originalPattern;
    }

    public QuadFilterPatternCanonical getCanonicalPattern() {
        return canonicalPattern;
    }

//    public Set<Set<Expr>> getFilterCnf() {
//        return filterCnf;
//    }

    public IBiSetMultimap<Quad, Set<Set<Expr>>> getQuadToCnf() {
        return quadToCnf;
    }
//    public Set<Set<Set<Expr>>> getQuadCnfs() {
//        return quadCnfs;
//    }

    public IBiSetMultimap<Var, VarOccurrence> getVarOccurrences() {
        return varOccurrences;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((canonicalPattern == null) ? 0 : canonicalPattern.hashCode());
        result = prime * result
                + ((originalPattern == null) ? 0 : originalPattern.hashCode());
        result = prime * result
                + ((quadToCnf == null) ? 0 : quadToCnf.hashCode());
        result = prime * result
                + ((varOccurrences == null) ? 0 : varOccurrences.hashCode());
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
        PatternSummary other = (PatternSummary) obj;
        if (canonicalPattern == null) {
            if (other.canonicalPattern != null)
                return false;
        } else if (!canonicalPattern.equals(other.canonicalPattern))
            return false;
        if (originalPattern == null) {
            if (other.originalPattern != null)
                return false;
        } else if (!originalPattern.equals(other.originalPattern))
            return false;
        if (quadToCnf == null) {
            if (other.quadToCnf != null)
                return false;
        } else if (!quadToCnf.equals(other.quadToCnf))
            return false;
        if (varOccurrences == null) {
            if (other.varOccurrences != null)
                return false;
        } else if (!varOccurrences.equals(other.varOccurrences))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PatternSummary [originalPattern=" + originalPattern
                + ", canonicalPattern=" + canonicalPattern + ", quadToCnf="
                + quadToCnf + ", varOccurrences=" + varOccurrences + "]";
    }
}
