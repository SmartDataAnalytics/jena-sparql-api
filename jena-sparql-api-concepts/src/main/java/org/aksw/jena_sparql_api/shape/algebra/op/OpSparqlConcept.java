package org.aksw.jena_sparql_api.shape.algebra.op;

import org.aksw.jena_sparql_api.concepts.Concept;

public class OpSparqlConcept
    extends Op0
{
    protected Concept concept;

    public OpSparqlConcept(Concept concept) {
        super();
        this.concept = concept;
    }

    public Concept getConcept() {
        return concept;
    }

    @Override
    public <T> T accept(OpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

}
