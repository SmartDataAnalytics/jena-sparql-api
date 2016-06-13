package org.aksw.jena_sparql_api.concept.builder.impl;

import org.aksw.jena_sparql_api.concept.builder.api.NodeBuilder;
import org.aksw.jena_sparql_api.concept.builder.api.TreeBuilder;

public class TreeBuilderImpl
    implements TreeBuilder
{
    protected NodeBuilder source;
    protected NodeBuilder predicate;
    protected NodeBuilder target;
    protected boolean isOptional;



    public TreeBuilderImpl(NodeBuilder source, NodeBuilder predicate, NodeBuilder target, boolean isOptional) {
        super();
        this.source = source;
        this.predicate = predicate;
        this.target = target;
        this.isOptional = isOptional;
    }

    @Override
    public NodeBuilder getSource() {
        return source;
    }

    @Override
    public NodeBuilder getPredicate() {
        return predicate;
    }

    @Override
    public NodeBuilder getTarget() {
        return target;
    }

    @Override
    public boolean isOptional() {
        return this.isOptional;
    }

    @Override
    public TreeBuilder setOptional(boolean b) {
        this.isOptional = b;
        return this;
    }
}
