package org.aksw.jena_sparql_api.concept.builder.owlapi;

import java.util.stream.Stream;

import org.aksw.jena_sparql_api.concept.builder.api.ConceptBuilder;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptBuilderExt;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptBuilderList;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptBuilderVisitor;
import org.aksw.jena_sparql_api.concept.builder.impl.ConceptBuilderAnd;
import org.aksw.jena_sparql_api.concept.builder.impl.ConceptBuilderImpl;
import org.aksw.jena_sparql_api.concept.builder.impl.ConceptBuilderUnion;
import org.semanticweb.owlapi.model.OWLClassExpression;

import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectUnionOfImpl;

public class ConceptBuilderVisitorOwlClassExpression
    implements ConceptBuilderVisitor<OWLClassExpression>
{

    public Stream<OWLClassExpression> toStream(ConceptBuilderList cb) {
        Stream<OWLClassExpression> result = toStream(cb.getMembers().stream());
        return result;
    }

    public Stream<OWLClassExpression> toStream(Stream<? extends ConceptBuilder> cbs) {
        Stream<OWLClassExpression> result = cbs.map(cb -> cb.accept(this));
        return result;
    }



    @Override
    public OWLClassExpression visit(ConceptBuilderImpl cb) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OWLObjectUnionOfImpl visit(ConceptBuilderAnd cb) {
        OWLObjectUnionOfImpl result = new OWLObjectUnionOfImpl(toStream(cb));
        return result;
    }

    @Override
    public OWLObjectIntersectionOfImpl visit(ConceptBuilderUnion cb) {
        OWLObjectIntersectionOfImpl result = new OWLObjectIntersectionOfImpl(toStream(cb));
        return result;
    }

    @Override
    public OWLClassExpression visit(ConceptBuilderExt cb) {
        throw new UnsupportedOperationException("not implemented");
    }

}
