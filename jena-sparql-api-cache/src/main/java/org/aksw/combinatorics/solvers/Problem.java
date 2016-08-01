package org.aksw.combinatorics.solvers;

/**
 * A problem is an abstract entity that supports generation of (partial) solutions together with an estimated cost of doing so.
 * The cost should thereby be proportional to the number of solutions returned, because:
 * the more solution candidates there are, the more work has to be performed to check them all.
 *
 * Usually, a problem is backed by an equivalence class of items,
 * which are the basis for generating solutions.
 * Note, that the framework does not care about the nature of the items and solutions.
 *
 * @author Claus Stadler
 *
 * @param <S> The solution type
 */
public interface Problem<S>
    extends GenericProblem<S, Problem<S>>
{
}


