package org.aksw.jena_sparql_api.concept.builder.impl;

import org.aksw.jena_sparql_api.concept.builder.api.ConceptExpr;
import org.aksw.jena_sparql_api.concept.builder.api.RestrictionExprExists;
import org.aksw.jena_sparql_api.concept.builder.api.RestrictionExprVisitor;

public class RestrictionExprExistsImpl
    extends RestrictionExprBase
    implements RestrictionExprExists
{
    public RestrictionExprExistsImpl(ConceptExpr role, ConceptExpr filler) {
        super(role, filler);
    }

    @Override
    public <T> T accept(RestrictionExprVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
