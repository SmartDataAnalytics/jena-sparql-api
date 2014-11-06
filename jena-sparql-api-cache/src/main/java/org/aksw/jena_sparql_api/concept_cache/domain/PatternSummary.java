package org.aksw.jena_sparql_api.concept_cache.domain;

import java.util.Set;

import org.aksw.commons.collections.multimaps.IBiSetMultimap;

import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;



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


}
