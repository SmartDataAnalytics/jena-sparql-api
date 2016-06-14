package org.aksw.jena_sparql_api.concept.builder.impl;

import java.util.List;
import java.util.Set;

import org.aksw.jena_sparql_api.concept.builder.api.ConceptBuilder;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptExpr;
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

    /**
     * null if there is no base concept expr
     *
     */
    protected ConceptExpr baseConceptExpr;


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



    public ConceptExpr getBaseConceptExpr() {
        return baseConceptExpr;
    }



    /**
     * Sets a baseConceptBuilder and returns this (i.e. NOT the argument)
     *
     * @param baseConceptBuilder
     * @return
     */
    @Override
    public ConceptBuilder setBaseConceptExpr(ConceptExpr baseConceptExpr) {
        this.baseConceptExpr = baseConceptExpr;
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

}
