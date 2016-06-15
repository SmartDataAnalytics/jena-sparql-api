package org.aksw.jena_sparql_api.concept.builder.impl;

import org.aksw.jena_sparql_api.concept.builder.api.ConceptExpr;
import org.aksw.jena_sparql_api.concept.builder.api.RestrictionExprForAll;
import org.aksw.jena_sparql_api.concept.builder.api.RestrictionExprVisitor;

public class RestrictionExprForAllImpl
    extends RestrictionExprBase
    implements RestrictionExprForAll
{
    public RestrictionExprForAllImpl(ConceptExpr role, ConceptExpr filler) {
        super(role, filler);
    }

    @Override
    public <T> T accept(RestrictionExprVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

}
