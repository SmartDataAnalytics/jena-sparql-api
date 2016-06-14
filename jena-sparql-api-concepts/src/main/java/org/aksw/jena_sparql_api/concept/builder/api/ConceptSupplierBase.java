package org.aksw.jena_sparql_api.concept.builder.api;

import org.aksw.jena_sparql_api.concepts.Concept;

public class ConceptSupplierBase
    implements ConceptSupplier
{
    @Override
    public Object getType() {
        return ConceptSupplier.UNKNOWN;
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
    public Concept asConcept() {
        throw new IllegalStateException();
    }

    @Override
    public ConceptBuilder asBuilder() {
        throw new IllegalStateException();
    }

    @Override
    public Object asObject() {
        throw new IllegalStateException();
    }
}
