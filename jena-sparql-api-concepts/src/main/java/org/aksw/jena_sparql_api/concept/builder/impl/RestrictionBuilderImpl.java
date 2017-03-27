package org.aksw.jena_sparql_api.concept.builder.impl;

import org.aksw.jena_sparql_api.concept.builder.api.ConceptBuilder;
import org.aksw.jena_sparql_api.concept.builder.api.RestrictionBuilder;
import org.aksw.jena_sparql_api.concept.builder.api.RestrictionExpr;
import org.aksw.jena_sparql_api.concept.builder.api.RestrictionExprQuantor;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

public class RestrictionBuilderImpl
    implements RestrictionBuilder
{
    protected ConceptBuilderImpl parent;

    protected Node on = null;
    //protected Set<>
    protected Var alias; // In the future we could allow a sets of aliases
    protected ConceptBuilder conceptBuilder;

    public RestrictionBuilderImpl(ConceptBuilderImpl parent) {
        this.parent = parent;
    }


    @Override
    public RestrictionBuilderImpl on(Node node) {
        this.on = node;
        return this;
    }


    @Override
    public ConceptBuilder forAll() {
        ConceptBuilder result = new ConceptBuilderImpl(this);
        return result;
    }


    @Override
    public ConceptBuilder exists() {
        ConceptBuilder result = new ConceptBuilderImpl(this);
        return result;
    }


    @Override
    public RestrictionBuilder as(Var var) {
        this.alias = var;
        return this;
    }


    /**
     * Remove this restriction from its owning concept.
     * Returns the parent concept
     *
     */
    @Override
    public ConceptBuilder destroy() {
        if(on != null) {
            parent.nodeToRestrictionBuilder.remove(on, this);
            this.parent = null;
        }
        return parent;
    }


    @Override
    public ConceptBuilder getParent() {
        return parent;
    }


    @Override
    public RestrictionExpr get() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public RestrictionExprQuantor build() {
        // TODO Auto-generated method stub
        return null;
    }
}
