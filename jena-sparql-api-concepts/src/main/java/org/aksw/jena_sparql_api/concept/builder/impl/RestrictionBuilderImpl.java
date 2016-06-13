package org.aksw.jena_sparql_api.concept.builder.impl;

import org.aksw.jena_sparql_api.concept.builder.api.ConceptBuilder;
import org.aksw.jena_sparql_api.concept.builder.api.RestrictionBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

public class RestrictionBuilderImpl
    implements RestrictionBuilder
{
    protected ConceptBuilderBase parent;

    protected Node on = null;
    //protected Set<>
    protected Var alias; // In the future we could allow a sets of aliases
    protected ConceptBuilder conceptBuilder;

    public RestrictionBuilderImpl(ConceptBuilderBase parent) {
        this.parent = parent;
    }


    @Override
    public RestrictionBuilderImpl on(Node node) {
        this.on = node;
        return this;
    }


    @Override
    public ConceptBuilder forAll() {
        ConceptBuilder result = new ConceptBuilderBase(this);
        return result;
    }


    @Override
    public ConceptBuilder exists() {
        ConceptBuilder result = new ConceptBuilderBase(this);
        return result;
    }


    @Override
    public RestrictionBuilder as(Var var) {
        this.alias = var;
        return this;
    }


    /**
     * Remove this restriction from the concept
     *
     */
    @Override
    public void destroy() {
        if(on != null) {
            parent.nodeToRestrictionBuilder.remove(on, this);
            this.parent = null;
        }
    }
}
