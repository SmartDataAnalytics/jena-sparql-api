package org.aksw.jena_sparql_api.concept.builder.api;

import org.aksw.jena_sparql_api.concepts.Concept;

public abstract class ConceptExprBase
    implements ConceptExpr
{
    @Override
    public Object getType() {
        return ConceptExpr.UNKNOWN;
    }

    @Override
    public boolean isConcept() {
        return false;
    }

    @Override
    public boolean isBuilder() {
        return false;
    }

    @Override
    public boolean isList() {
        return false;
    }

    @Override
    public Concept asConcept() {
        throw new IllegalStateException();
    }

    @Override
    public ConceptBuilder asBuilder() {
        throw new IllegalStateException();
    }

    @Override
    public ConceptExprList asList() {
        throw new IllegalStateException();
    }

    @Override
    public Object asObject() {
        throw new IllegalStateException();
    }
}
