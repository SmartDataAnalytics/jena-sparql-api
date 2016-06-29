package org.aksw.isomorphism;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;

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
    protected NavigableMap<? extends Comparable<?>, Collection<GenericProblem<S, ?>>> sizeToProblem;

    public ProblemContainerImpl(NavigableMap<? extends Comparable<?>, Collection<GenericProblem<S, ?>>> sizeToProblem) {
        super();
        this.sizeToProblem = sizeToProblem;
    }

//    public void add(GenericProblem<S, ?> problem) {
//        double cost = problem.getEstimatedCost();
//        sizeToProblem.put(cost, problem);
//    }
//
//    public void addAll(Iterable<GenericProblem<S, ?>> problems) {
//        problems.forEach(x -> add(x));
//    }
//

    /**
     * Pick the estimated cheapest problem,
     * return a a problem collection with that generator removed
     *
     * @return
     */
    public ProblemContainerPick<S> pick() {
        ProblemContainerPick<S> result;

        Entry<? extends Comparable<?>, Collection<GenericProblem<S, ?>>> currEntry = sizeToProblem.firstEntry();
        if(currEntry != null) {
            Comparable<?> pickedKey = currEntry.getKey();
            GenericProblem<S, ?> pickedProblem = Iterables.getFirst(currEntry.getValue(), null);




            NavigableMap<Comparable<?>, Collection<GenericProblem<S, ?>>> remaining = new TreeMap<>();
            sizeToProblem.forEach((k, v) -> {
                Collection<GenericProblem<S, ?>> ps = v.stream().filter(i -> i != pickedProblem).collect(Collectors.toList());
                remaining.computeIfAbsent(k, (x) -> new ArrayList<GenericProblem<S, ?>>()).addAll(ps);
            });
                    //ArrayListMultimap.create(sizeToProblem);
            //remaining.remove(pickedKey, pickedProblem);

            ProblemContainerImpl<S> r = new ProblemContainerImpl<>(remaining);

            result = null; //new ProblemContainerPick<>(pickedProblem, r);
        } else {
            throw new IllegalStateException();
        }

        return result;
    }

    /**
     * Partition the content of the collection in regard to a partial solution.
     * This operation may return 'this'
     *
     * @param partialSolution
     */
    public ProblemContainerImpl<S> refine(S partialSolution) {
        Collection<GenericProblem<S, ?>> tmp = sizeToProblem.values().stream()
            .flatMap(x -> x.stream())
            .map(problem -> problem.refine(partialSolution))
            .flatMap(x -> x.stream())
            .collect(Collectors.toList());

        NavigableMap<Long, Collection<GenericProblem<S, ?>>> sizeToProblem = IsoUtils.indexSolutionGenerators(tmp);
        ProblemContainerImpl<S> result = new ProblemContainerImpl<S>(sizeToProblem);

        return result;
    }

    @Override
    public boolean isEmpty() {
        boolean result = sizeToProblem.isEmpty();
        return result;
    }


    @SafeVarargs
    public static <S> ProblemContainerImpl<S> create(GenericProblem<S, ?> ... problems) {
        Collection<GenericProblem<S, ?>> tmp = Arrays.asList(problems);
        ProblemContainerImpl<S> result = create(tmp);
        return result;
    }

    public static <S> ProblemContainerImpl<S> create(Collection<GenericProblem<S, ?>> problems) {
        NavigableMap<Long, Collection<GenericProblem<S, ?>>> sizeToProblem = IsoUtils.indexSolutionGenerators(problems);
        ProblemContainerImpl<S> result = new ProblemContainerImpl<>(sizeToProblem);
        return result;
    }
}