package org.aksw.isomorphism;

import org.aksw.jena_sparql_api.views.GenericNestedStack;

public class ClusterStack<A, B, S>
    extends GenericNestedStack<Cluster<A, B, S>, ClusterStack<A, B, S>>
{
    public ClusterStack(ClusterStack<A, B, S> parent, Cluster<A, B, S> value) {
        super(parent, value);
    }
}