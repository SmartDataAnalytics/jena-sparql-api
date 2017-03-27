package org.aksw.jena_sparql_api.concept.builder.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.concept.builder.api.ConceptBuilder;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptExpr;
import org.aksw.jena_sparql_api.concept.builder.api.RestrictionBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.Expr;

public class ConceptBuilderImpl
    implements ConceptBuilder
{
    protected RestrictionBuilder parent;

    protected boolean isNegated = false;

    protected Map<Node, RestrictionBuilder> nodeToRestrictionBuilder = new HashMap<>();
    //protected List<RestrictionBuilde>

    /**
     * null if there is no base concept expr
     *
     */
    protected ConceptExpr baseConceptExpr;


    protected Set<Expr> exprs;


    public ConceptBuilderImpl() {
        this(null);
    }


    public ConceptBuilderImpl(RestrictionBuilder parent) {
        super();
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
    public ConceptBuilderImpl setNegated(boolean status) {
        this.isNegated = true;
        return this;
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
    public RestrictionBuilder getParent() {
        return parent;
    }


    @Override
    public ConceptBuilderImpl clone() throws CloneNotSupportedException {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public ConceptBuilder removeExpr(Expr expr) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public Set<Expr> getExprs() {
        return exprs;
    }


    @Override
    public Collection<RestrictionBuilder> listRestrictions() {
        Collection<RestrictionBuilder> result = nodeToRestrictionBuilder.values();
        return result;
    }


}
