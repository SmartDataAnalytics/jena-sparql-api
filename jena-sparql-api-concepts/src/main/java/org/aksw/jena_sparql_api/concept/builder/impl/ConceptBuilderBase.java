package org.aksw.jena_sparql_api.concept.builder.impl;

import java.util.List;
import java.util.Set;

import org.aksw.jena_sparql_api.concept.builder.api.ConceptBuilder;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptSupplier;
import org.aksw.jena_sparql_api.concept.builder.api.RestrictionBuilder;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.Expr;

import com.google.common.collect.ListMultimap;

public abstract class ConceptBuilderBase
    implements ConceptBuilder
{
    protected RestrictionBuilder parent;
    protected ListMultimap<Node, RestrictionBuilder> nodeToRestrictionBuilder;
    //protected List<RestrictionBuilde>

    protected ConceptSupplier baseConceptBuilder;


    /**
     * An optional SPARQL concept
     */
    protected Concept sparqlConcept;
    protected Set<Expr> exprs;


//    public ConceptBuilderImpl() {
//
//    }


    public ConceptBuilderBase(RestrictionBuilder parent) {
        this.parent = parent;
    }



    public ConceptBuilder getBaseConceptBuilder() {
        return baseConceptBuilder;
    }



    /**
     * Sets a baseConceptBuilder and returns this (i.e. NOT the argument)
     *
     * @param baseConceptBuilder
     * @return
     */
    @Override
    public ConceptBuilder setBaseConceptBuilder(ConceptBuilder baseConceptBuilder) {
        this.baseConceptBuilder = baseConceptBuilder;
        return this;
    }



    @Override
    public RestrictionBuilder newRestriction() {
        RestrictionBuilder result = new RestrictionBuilderImpl(this);
        return result;
    }



    @Override
    public List<RestrictionBuilder> findRestrictions(Node node) {
        // TODO Auto-generated method stub
        return null;
    }



    @Override
    public void setNegated(boolean status) {
        // TODO Auto-generated method stub

    }



    @Override
    public void isNegated() {
        // TODO Auto-generated method stub

    }


    @Override
    public ConceptBuilder addExpr(Expr expr) {
        this.exprs.add(expr);
        return this;
    }

    @Override
    public Concept get() {
        Concept baseConcept = baseConceptBuilder == null
                ? null
                : baseConceptBuilder.get();

        // Create a concept from the restrictions and merge them


        Concept result = baseConcept;
        return result;
    }


}
