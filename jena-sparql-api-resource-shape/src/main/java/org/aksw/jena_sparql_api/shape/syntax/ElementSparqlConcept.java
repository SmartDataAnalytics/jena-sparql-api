package org.aksw.jena_sparql_api.shape.syntax;

import org.aksw.jena_sparql_api.concepts.Concept;

public class ElementSparqlConcept
    extends Element0
{
    protected Concept concept;

    public ElementSparqlConcept(Concept concept) {
        super();
        this.concept = concept;
    }

    public Concept getConcept() {
        return concept;
    }

    @Override
    public <T> T accept(ElementVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
