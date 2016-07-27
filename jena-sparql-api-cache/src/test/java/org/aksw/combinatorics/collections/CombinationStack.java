package org.aksw.combinatorics.collections;

import org.aksw.commons.collections.stacks.GenericNestedStack;

/**
 * Helper type to avoid having to type NestedStack<Combination2<A, B, S>> all the time.
 * Makes the API more user friendly.
 *
 * @author raven
 *
 * @param <A>
 * @param <B>
 * @param <S>
 */
public class CombinationStack<A, B, S>
    extends GenericNestedStack<Combination<A, B, S>, CombinationStack<A, B, S>>
{
    public CombinationStack(CombinationStack<A, B, S> parent, Combination<A, B, S> value) {
        super(parent, value);
    }
}
