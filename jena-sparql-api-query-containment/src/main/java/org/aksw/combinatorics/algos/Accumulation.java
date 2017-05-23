package org.aksw.combinatorics.algos;

import org.aksw.commons.collections.stacks.NestedStack;

public class Accumulation<A, B, C, S> {
    protected S initialSolution;
    protected NestedStack<AccumulationEntry<A, B, C, S>> stack;
    
}
