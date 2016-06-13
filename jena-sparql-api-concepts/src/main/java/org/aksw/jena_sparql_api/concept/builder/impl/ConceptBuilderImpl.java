package org.aksw.jena_sparql_api.concept.builder.impl;

import org.aksw.jena_sparql_api.concept.builder.api.ConceptBuilderVisitor;
import org.aksw.jena_sparql_api.concepts.Concept;

public class ConceptBuilderImpl
    extends ConceptBuilderBase
{
    protected Concept concept;


    public ConceptBuilderImpl() {
        super(null);
    }

    public Concept getConcept() {
        return concept;
    }

    public ConceptBuilderImpl setConcept(Concept concept) {
        this.concept = concept;
        return this;
    }

    @Override
    public <T> T accept(ConceptBuilderVisitor<T> visitor) {
        visitor.visit(this);
    }

}
