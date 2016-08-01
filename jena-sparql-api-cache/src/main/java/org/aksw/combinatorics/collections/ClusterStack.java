package org.aksw.combinatorics.collections;

import org.aksw.commons.collections.stacks.GenericNestedStack;

public class ClusterStack<A, B, S>
    extends GenericNestedStack<Cluster<A, B, S>, ClusterStack<A, B, S>>
{
    public ClusterStack(ClusterStack<A, B, S> parent, Cluster<A, B, S> value) {
        super(parent, value);
    }
}