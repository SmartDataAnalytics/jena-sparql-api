package org.aksw.isomorphism;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;
import com.google.common.collect.TreeMultimap;

/**
 * A simple cost based problem container implementation.
 *
 * Note: At present it does not handle dependencies between problems.
 * For instance, if an edge in graph falls into one equivalence class, then in the next step
 * the edge's neighbors (and only those) should be examined of whether they should be processed next.
 *
 * Right now, we attempt to refine all problems, instead of using information
 * about which ones are actually affected.
 *
 *
 * @author Claus Stadler
 *
 * @param <S> The solution type
 */
public class ProblemContainerImpl<S>
    implements ProblemContainer<S>
{
    protected TreeMultimap<? extends Comparable<?>, Problem<S>> sizeToProblem;

    public ProblemContainerImpl(
            TreeMultimap<? extends Comparable<?>, Problem<S>> sizeToProblem) {
        super();
        this.sizeToProblem = sizeToProblem;
    }

    /**
     * Pick the estimated cheapest problem,
     * return a a problem collection with that generator removed
     *
     * @return
     */
    public ProblemContainerPick<S> pick() {
        Entry<? extends Comparable<?>, Collection<Problem<S>>> currEntry = sizeToProblem.asMap().firstEntry();
        Comparable<?> pickedKey = currEntry.getKey();
        Problem<S> pickedProblem = Iterables.getFirst(currEntry.getValue(), null);

        TreeMultimap<Comparable<?>, Problem<S>> remaining = TreeMultimap.create(sizeToProblem);
        remaining.remove(pickedKey, pickedProblem);

        ProblemContainerImpl<S> r = new ProblemContainerImpl<>(remaining);

        ProblemContainerPick<S> result = new ProblemContainerPick<>(pickedProblem, r);
        return result;
    }

    /**
     * Partition the content of the collection in regard to a partial solution.
     * This operation may return 'this'
     *
     * @param partialSolution
     */
    public ProblemContainerImpl<S> refine(S partialSolution) {
        Collection<Problem<S>> tmp = sizeToProblem.values().stream()
            .map(problem -> problem.refine(partialSolution))
            .flatMap(x -> x.stream())
            .collect(Collectors.toList());

        TreeMultimap<Long, Problem<S>> sizeToProblem = IsoUtils.indexSolutionGenerators(tmp);
        ProblemContainerImpl<S> result = new ProblemContainerImpl<S>(sizeToProblem);

        return result;
    }

    @Override
    public boolean isEmpty() {
        boolean result = sizeToProblem.isEmpty();
        return result;
    }
}