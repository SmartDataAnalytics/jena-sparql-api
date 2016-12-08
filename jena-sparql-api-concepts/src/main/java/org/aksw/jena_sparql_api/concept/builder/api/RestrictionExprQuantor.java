package org.aksw.jena_sparql_api.concept.builder.api;

public interface RestrictionExprQuantor
    extends RestrictionExpr
{
    ConceptExpr getRole();
    ConceptExpr getFiller();    
}
