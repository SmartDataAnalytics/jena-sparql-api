package org.aksw.jena_sparql_api.concept.builder.owlapi;

import java.util.Set;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;

public class OWLConceptImpl
    implements OWLClassExpression
{

    @Override
    public void accept(OWLObjectVisitor visitor) {
        // TODO Auto-generated method stub

    }

    @Override
    public <O> O accept(OWLObjectVisitorEx<O> visitor) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int compareTo(OWLObject o) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int typeIndex() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int hashIndex() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Stream<?> components() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ClassExpressionType getClassExpressionType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isOWLThing() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isOWLNothing() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public OWLClassExpression getNNF() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OWLClassExpression getComplementNNF() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OWLClassExpression getObjectComplementOf() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<OWLClassExpression> asConjunctSet() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean containsConjunct(OWLClassExpression ce) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Set<OWLClassExpression> asDisjunctSet() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void accept(OWLClassExpressionVisitor visitor) {
        // TODO Auto-generated method stub

    }

    @Override
    public <O> O accept(OWLClassExpressionVisitorEx<O> visitor) {
        // TODO Auto-generated method stub
        return null;
    }

}
