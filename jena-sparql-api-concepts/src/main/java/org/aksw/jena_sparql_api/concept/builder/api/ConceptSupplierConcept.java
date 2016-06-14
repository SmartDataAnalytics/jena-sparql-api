package org.aksw.jena_sparql_api.concept.builder.api;

import org.aksw.jena_sparql_api.concepts.Concept;

public class ConceptSupplierConcept
    extends ConceptSupplierBase
{
    protected Concept concept;

    public ConceptSupplierConcept(Concept concept) {
        super();
        this.concept = concept;
    }

    public Concept getConcept() {
        return concept;
    }

    @Override
    public Object getType() {
        return ConceptSupplier.CONCEPT;
    }

    @Override
    public boolean isConcept() {
        return true;
    }

    @Override
    public Concept asConcept() {
        return concept;
    }

    @Override
    public Concept asObject() {
        return concept;
    }
}
