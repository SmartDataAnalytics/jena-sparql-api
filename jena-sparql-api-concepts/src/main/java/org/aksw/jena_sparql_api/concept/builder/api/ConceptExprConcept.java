package org.aksw.jena_sparql_api.concept.builder.api;

import org.aksw.jena_sparql_api.concepts.Concept;

public class ConceptExprConcept
    extends ConceptExprBase
{
    protected Concept concept;

    public ConceptExprConcept(Concept concept) {
        super();
        this.concept = concept;
    }

    public Concept getConcept() {
        return concept;
    }

    @Override
    public Object getType() {
        return ConceptExpr.CONCEPT;
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

    @Override
    public <T> T accept(ConceptExprVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
