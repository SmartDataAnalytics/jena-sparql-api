package org.aksw.isomorphism;

/**
 * Abstraction over a collection of problems.
 * Implementations of this interface *must* be capable of picking one of its contained
 * problems, which *should* be the cheapest one (in regard to
 * some estimate). Also, the pick method must return the remaining workload
 * again abstracted as a problem collection.
 *
 * Essentially, the problem container represents a state, and the pick method
 * performs the transition to a new state.
 *
 * The {@link ProblemSolver} is the class that actively invokes the the pick
 * and refine methods.
 *
 *
 * @author Claus Stadler
 *
 * @param <S> The solution type
 */
public interface ProblemContainer<S> {
    ProblemContainer<S> refine(S partialSolution);
    ProblemContainerPick<S> pick();
    boolean isEmpty();
}
