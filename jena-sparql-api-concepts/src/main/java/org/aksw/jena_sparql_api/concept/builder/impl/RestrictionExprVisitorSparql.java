package org.aksw.jena_sparql_api.concept.builder.impl;

import org.aksw.jena_sparql_api.concept.builder.api.RestrictionExprExists;
import org.aksw.jena_sparql_api.concept.builder.api.RestrictionExprExt;
import org.aksw.jena_sparql_api.concept.builder.api.RestrictionExprForAll;
import org.aksw.jena_sparql_api.concept.builder.api.RestrictionExprVisitor;
import org.aksw.jena_sparql_api.concepts.Concept;

public class RestrictionExprVisitorSparql
    implements RestrictionExprVisitor<Concept>
{
    @Override
    public Concept visit(RestrictionExprExists re) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Concept visit(RestrictionExprForAll re) {
        // count the number of values for the given role concept        
        // count the number of values for the given role that satisfy the fille
        // check whether the counts are equal
        
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Concept visit(RestrictionExprExt re) {
        throw new UnsupportedOperationException();
    }
}
