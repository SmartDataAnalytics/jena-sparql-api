package org.aksw.jena_sparql_api.concept.builder.api;

public class ConceptExprConceptBuilder
    extends ConceptExprBase
{
    protected ConceptBuilder conceptBuilder;

    public ConceptExprConceptBuilder(ConceptBuilder conceptBuilder) {
        super();
        this.conceptBuilder = conceptBuilder;
    }

    public ConceptBuilder getConceptBuilder() {
        return conceptBuilder;
    }

    @Override
    public Object getType() {
        return ConceptExpr.CONCEPT;
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

    @Override
    public <T> T accept(ConceptExprVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
