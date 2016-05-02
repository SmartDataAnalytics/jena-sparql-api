package org.aksw.isomorphism;

import java.util.Collection;
import java.util.stream.Stream;

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
    extends Comparable<Problem<S>>
{
    /**
     * Report an estimate cost for solving the problem with its current setup.
     * Note, that the cost computation should be cheap.
     * @return
     */
    long getEstimatedCost();

    /**
     * Return a stream of solutions
     *
     * Note: if generate solutions should operate on a partial solution, use refine first
     *
     * @return
     */
    Stream<S> generateSolutions();

    /**
     * Refine the problem by a partial solution
     *
     * @param partialSolution
     * @return
     */
    Collection<Problem<S>> refine(S partialSolution);

    /**
     * By default, compares the estimated costs
     */
    @Override
    default int compareTo(Problem<S> o) {
        int result;

        if(o == null) {
            result = 1; // Sort nulls first
        } else {
            long a = this.getEstimatedCost();
            long b = o.getEstimatedCost();
            result = Long.compare(a, b);
        }
        return result;
    }
}


