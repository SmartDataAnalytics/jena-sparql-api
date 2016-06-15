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
import org.aksw.jena_sparql_api.utils.Generator;
import org.apache.jena.sparql.core.Var;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;


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
                : ConceptOps.intersect(baseConcept, concept);

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
            .reduce(Concept.TOP, (a, b) -> ConceptOps.intersect(a, b));

        return result;
    }

    @Override
    public Concept visit(RestrictionExprExists re) {        
        String objectVariable = mapping.newIndividualVariable();
        OWLObjectPropertyExpression propertyExpression = ce.getProperty();
        if(propertyExpression.isAnonymous()){
            //property expression is inverse of a property
            sparql += asTriplePattern(objectVariable, propertyExpression.getNamedProperty(), variables.peek());
        } else {
            sparql += asTriplePattern(variables.peek(), propertyExpression.getNamedProperty(), objectVariable);
        }
        OWLClassExpression filler = ce.getFiller();
        variables.push(objectVariable);
        filler.accept(this);
        variables.pop();
    }

    @Override
    public Concept visit(RestrictionExprForAll re) {
        Generator<Var> varGen = null;
        ConceptExpr filler = re.getFiller();
        
        
        Var subject = varGen.current();
        Var object = varGen.next();
        
        if(isTrivialConcept(filler)) { 
            // \forall r.\top is trivial, as everything belongs to that concept
            // thus, we can omit it if it's used in a conjunction or as complex filler
            if(!inIntersection()) {
                sparql += asTriplePattern(subject, mapping.newPropertyVariable(), objectVariable);
            }
        } else {
            if(!inIntersection()) {
                sparql += asTriplePattern(subject, mapping.newPropertyVariable(), objectVariable);
            }
            // we can either use double negation on \forall r.A such that we have a logically
            // equivalent expression \neg \exists r.\neg A
            // or we use subselects get the individuals whose r successors are only of type A
            if(allQuantorTranslation == AllQuantorTranslation.DOUBLE_NEGATION){
                OWLObjectComplementOf doubleNegatedExpression = df.getOWLObjectComplementOf(
                        df.getOWLObjectSomeValuesFrom(
                                ce.getProperty(), 
                                df.getOWLObjectComplementOf(ce.getFiller())));
                doubleNegatedExpression.accept(this);
            } else {
                OWLObjectPropertyExpression propertyExpression = ce.getProperty();
                OWLObjectProperty predicate = propertyExpression.getNamedProperty();
                if(propertyExpression.isAnonymous()){
                    //property expression is inverse of a property
                    sparql += asTriplePattern(objectVariable, predicate, variables.peek());
                } else {
                    sparql += asTriplePattern(variables.peek(), predicate, objectVariable);
                }
                
                String var = mapping.newIndividualVariable();
                sparql += "{SELECT " + subject + " (COUNT(" + var + ") AS ?cnt1) WHERE {";
                sparql += asTriplePattern(subject, predicate, var);
                variables.push(var);
                filler.accept(this);
                variables.pop();
                sparql += "} GROUP BY " + subject + "}";
                
                var = mapping.newIndividualVariable();
                sparql += "{SELECT " + subject + " (COUNT(" + var + ") AS ?cnt2) WHERE {";
                sparql += asTriplePattern(subject, predicate, var);
                sparql += "} GROUP BY " + subject + "}";
                
                sparql += filter("?cnt1=?cnt2");
            }
        }    }

    @Override
    public Concept visit(RestrictionExprExt re) {
        throw new UnsupportedOperationException("subclass the visitor to handle custom types");
    }
}
