package org.aksw.jena_sparql_api.concept.builder.impl;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.concept.builder.api.ConceptBuilder;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptExpr;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptExprConcept;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptExprConceptBuilder;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptExprExt;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptExprList;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptExprVisitor;
import org.aksw.jena_sparql_api.concept.builder.api.RestrictionBuilder;
import org.aksw.jena_sparql_api.concept.builder.api.RestrictionExprExists;
import org.aksw.jena_sparql_api.concept.builder.api.RestrictionExprExt;
import org.aksw.jena_sparql_api.concept.builder.api.RestrictionExprForAll;
import org.aksw.jena_sparql_api.concept.builder.api.RestrictionExprVisitor;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptOps;


/**
 * Expression visitor that generates a SPARQL concept from a concept expression
 *
 * @author raven
 *
 */
public class ConceptExprVisitorSparql
    implements ConceptExprVisitor<Concept>, RestrictionExprVisitor<Concept>
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

        Concept concept = createConceptFromRestrictions(cb);

        Concept result = baseConcept == null
                ? concept
                : ConceptOps.intersect(baseConcept, concept, null);

        return result;
    }

    @Override
    public Concept visit(ConceptExprExt cse) {
        throw new UnsupportedOperationException("subclass the visitor to handle custom types");
    }

    @Override
    public Concept visit(ConceptExprList ce) {
        List<Concept> concepts = ce.getMembers().stream().map(x -> x.accept(this)).collect(Collectors.toList());

        Concept result = ce.isUnionMode()
                ? ConceptOps.union(concepts.stream())
                : ConceptOps.intersect(concepts.stream());

        return result;
    }


    public Concept createConceptFromRestrictions(ConceptBuilder cb) {
        Collection<RestrictionBuilder> rbs = cb.listRestrictions();

        Concept result = rbs.stream()
            .map(rb -> rb.get())
            .map(re -> re.accept(this))
            .reduce(Concept.TOP, (a, b) -> ConceptOps.intersect(a, b, null));

        return result;
    }

    @Override
    public Concept visit(RestrictionExprExists re) {
        Concept r = re.getRole().accept(this);

        ConceptExpr fillerCe = re.getFiller();
        Concept filler = fillerCe.accept(this);

        Concept result = ConceptOps.intersect(r, filler, null);
        return result;
    }

    @Override
    public Concept visit(RestrictionExprForAll re) {
        return null;
    }

    @Override
    public Concept visit(RestrictionExprExt re) {
        throw new UnsupportedOperationException("subclass the visitor to handle custom types");
    }
}
