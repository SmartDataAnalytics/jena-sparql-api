package org.aksw.jena_sparql_api.concept.builder.impl;

import org.aksw.jena_sparql_api.concept.builder.api.ConceptExpr;
import org.aksw.jena_sparql_api.concept.builder.api.RestrictionExprQuantor;

public abstract class RestrictionExprBase
    implements RestrictionExprQuantor
{
    protected ConceptExpr role;
    protected ConceptExpr filler;
    
//    public RestrictionExprBase() {
//        this(null, null);
//    }
    
    public RestrictionExprBase(ConceptExpr role, ConceptExpr filler) {
        super();
        this.role = role;
        this.filler = filler;
    }

    @Override
    public ConceptExpr getRole() {
        return role;
    }

    @Override
    public ConceptExpr getFiller() {
        return filler;
    }

}
