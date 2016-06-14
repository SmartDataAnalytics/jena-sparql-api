package org.aksw.jena_sparql_api.concept.builder.impl;

import org.aksw.jena_sparql_api.concept.builder.api.ConceptBuilder;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptExpr;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptExprConcept;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptExprConceptBuilder;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptExprExt;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptExprVisitor;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptOps;


/**
 * Expression visitor that generates a SPARQL concept from a concept expression
 *
 * @author raven
 *
 */
public class ConceptExprVisitorSparql
    implements ConceptExprVisitor<Concept>
{
    @Override
    public Concept visit(ConceptExprConcept ce) {
        Concept result = ce.getConcept();
        return result;
    }

    @Override
    public Concept visit(ConceptExprConceptBuilder ce) {
        ConceptBuilder cb = ce.getConceptBuilder();

        ConceptExpr baseConceptExpr = cb.getBaseConceptExpr();
        Concept baseConcept = baseConceptExpr == null
                ? null
                : baseConceptExpr.accept(this);

        Concept concept = cb.accept(this);

        Concept result = baseConcept == null
                ? concept
                : ConceptOps.intersect(baseConcept, concept);

        return result;
    }

    @Override
    public Concept visit(ConceptExprExt cse) {
        throw new UnsupportedOperationException("subclass the visitor to handle custom types");
    }

}
