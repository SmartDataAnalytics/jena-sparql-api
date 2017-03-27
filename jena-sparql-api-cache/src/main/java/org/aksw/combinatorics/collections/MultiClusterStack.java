package org.aksw.combinatorics.collections;

import java.util.Collection;

import org.aksw.commons.collections.stacks.GenericNestedStack;

public class MultiClusterStack<A, B, S>
    extends GenericNestedStack<Collection<? extends Cluster<A, B, S>>, MultiClusterStack<A, B, S>>
{    
    public MultiClusterStack(MultiClusterStack<A, B, S> parent,
            Collection<? extends Cluster<A, B, S>> value) {
        super(parent, value);
    }
}