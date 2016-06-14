package org.aksw.jena_sparql_api.concept.builder.api;

public class ConceptSupplierConceptBuilder
    extends ConceptSupplierBase
{
    protected ConceptBuilder conceptBuilder;

    public ConceptSupplierConceptBuilder(ConceptBuilder conceptBuilder) {

        super();
        this.conceptBuilder = conceptBuilder;
    }

    public ConceptBuilder getConceptBuilder() {
        return conceptBuilder;
    }

    @Override
    public Object getType() {
        return ConceptSupplier.CONCEPT;
    }

    @Override
    public boolean isBuilder() {
        return true;
    }

    @Override
    public ConceptBuilder asBuilder() {
        return conceptBuilder;
    }

    @Override
    public ConceptBuilder asObject() {
        return conceptBuilder;
    }
}
