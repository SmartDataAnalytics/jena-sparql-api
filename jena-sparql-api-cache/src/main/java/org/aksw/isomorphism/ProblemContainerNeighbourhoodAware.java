package org.aksw.isomorphism;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;


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
public class ProblemContainerNeighbourhoodAware<S, T>
    implements ProblemContainer<S>
{
    protected Stack<Entry<? extends Comparable<?>, ProblemNeighbourhoodAware<S, T>>> stack = new Stack<>();


    protected NavigableMap<? extends Comparable<?>, Collection<ProblemNeighbourhoodAware<S, T>>> sizeToProblem;

    // Problems which are reachable via relatedness
    protected NavigableMap<? super Comparable<?>, Collection<ProblemNeighbourhoodAware<S, T>>> refinementQueue = new TreeMap<>();

    protected Multimap<T, ProblemNeighbourhoodAware<S, T>> sourceMapping = HashMultimap.create();
//    protected Multimap<T, ProblemNeighbourhoodAware<S, T>> targetMapping = HashMultimap.create();

    // Functions to extract source and target set
    protected Function<? super S, ? extends Collection<T>> getRelatedSources;
//    protected Function<? super S, ? extends Collection<T>> getRelatedTargets;



    public ProblemContainerNeighbourhoodAware(
            NavigableMap<? extends Comparable<?>, Collection<ProblemNeighbourhoodAware<S, T>>> sizeToProblem,
            Multimap<T, ProblemNeighbourhoodAware<S, T>> sourceMapping
//            Multimap<T, ProblemNeighbourhoodAware<S, T>> targetMapping
     ) {
        super();
        this.sizeToProblem = sizeToProblem;
        this.sourceMapping = sourceMapping;
//        this.targetMapping = targetMapping;
    }

//    public void add(Problem<S> problem) {
//        double cost = problem.getEstimatedCost();
//        sizeToProblem.put(cost, problem);
//    }
//
//    public void addAll(Iterable<Problem<S>> problems) {
//        problems.forEach(x -> add(x));
//    }
//

    // Move to multimap utils
    public static boolean remove(Map<?, ? extends Collection<?>> map, Object key, Object value) {
        boolean result = false;

        Collection<?> values = map.get(key);
        result = values == null ? false : values.remove(value);

        if(result) {
            if(values.isEmpty()) {
                map.remove(key);
            }
        }

        return result;
    }


    /**
     * Pick the estimated cheapest problem,
     * return a a problem collection with that generator removed
     *
     * @return
     */
    public ProblemContainerPick<S> pick() {
        ProblemContainerPick<S> result;

        Entry<? extends Comparable<?>, Collection<ProblemNeighbourhoodAware<S, T>>> currEntry = sizeToProblem.firstEntry();
        if(currEntry != null) {
            Comparable<?> pickedKey = currEntry.getKey();
            ProblemNeighbourhoodAware<S, T> pickedProblem = Iterables.getFirst(currEntry.getValue(), null);

            // remove the picked problem from the refinement queue if it is in it
            remove(refinementQueue, pickedKey, pickedProblem);
            remove(sizeToProblem, pickedKey, pickedProblem);

            Collection<T> sourceNeigbourhood = pickedProblem.getSourceNeighbourhood();
            sourceNeigbourhood.forEach(neighbour -> sourceMapping.remove(neighbour, pickedProblem));

            // recurse

            // restore state

//            Collection<T> tt = pickedProblem.exposeTargetNeighbourhood();
//            tt.forEach(t -> targetMapping.remove(t, pickedProblem));

            pickedProblem.generateSolutions().forEach(sol -> {
                Collection<T> rs = getRelatedSources.apply(sol);
                //Collection<T> rt = getRelatedTargets.apply(sol);
                Set<ProblemNeighbourhoodAware<S, T>> neighbours = rs
                        .stream()
                        .flatMap(s -> sourceMapping.get(s).stream())
                        .collect(Collectors.toSet());

                // add the neighbours to the queue
                neighbours.forEach(n -> {
                    long cost = n.getEstimatedCost();
                    refinementQueue.computeIfAbsent(cost, (x) -> new HashSet<>()).add(n);
                });


                // start refining unless there are open problems with a cost < 1



                // Add the neighbours to the queue


//                Stream.concat(
//                        rs.stream().flatMap(s -> sourceMapping.get(s).stream()),
//                        rt.stream().flatMap(t -> targetMapping.get(t).stream())
//                );
                //sourceMapping.get(key)


            });


            NavigableMap<Comparable<?>, Collection<Problem<S>>> remaining = new TreeMap<>();
            sizeToProblem.forEach((k, v) -> {
                Collection<Problem<S>> ps = v.stream().filter(i -> i != pickedProblem).collect(Collectors.toList());
                remaining.computeIfAbsent(k, (x) -> new ArrayList<Problem<S>>()).addAll(ps);
            });
                    //ArrayListMultimap.create(sizeToProblem);
            //remaining.remove(pickedKey, pickedProblem);

            //ProblemContainerNeighbourhoodAware<S> r = new ProblemContainerNeighbourhoodAware<>(remaining);
            ProblemContainerNeighbourhoodAware<S, T> r = null;

            result = new ProblemContainerPick<>(pickedProblem, r);
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
    public ProblemContainerNeighbourhoodAware<S, T> refine(S partialSolution) {
        Collection<Problem<S>> tmp = sizeToProblem.values().stream()
            .flatMap(x -> x.stream())
            .map(problem -> problem.refine(partialSolution))
            .flatMap(x -> x.stream())
            .collect(Collectors.toList());

        NavigableMap<Long, Collection<Problem<S>>> sizeToProblem = IsoUtils.indexSolutionGenerators(tmp);
        ProblemContainerNeighbourhoodAware<S, T> result = null; //ew ProblemContainerNeighbourhoodAware<S, T>(sizeToProblem);

        return result;
    }

    @Override
    public boolean isEmpty() {
        boolean result = sizeToProblem.isEmpty();
        return result;
    }


    @SafeVarargs
    public static <S, T> ProblemContainerNeighbourhoodAware<S, T> create(ProblemNeighbourhoodAware<S, T> ... problems) {
        Collection<ProblemNeighbourhoodAware<S, T>> tmp = Arrays.asList(problems);
        ProblemContainerNeighbourhoodAware<S, T> result = create(tmp);
        return result;
    }

    public static <S, T> ProblemContainerNeighbourhoodAware<S, T> create(Collection<ProblemNeighbourhoodAware<S, T>> problems) {
        NavigableMap<Long, Collection<ProblemNeighbourhoodAware<S, T>>> sizeToProblem = IsoUtils.indexSolutionGenerators(problems);

        Multimap<T, ProblemNeighbourhoodAware<S, T>> sourceMapping = HashMultimap.create();
        Multimap<T, ProblemNeighbourhoodAware<S, T>> targetMapping = HashMultimap.create();
        for(ProblemNeighbourhoodAware<S, T> p : problems) {
            Collection<T> sn = p.getSourceNeighbourhood();
            sn.forEach(v -> sourceMapping.put(v, p));

//            Collection<T> tn = p.exposeTargetNeighbourhood();
//            tn.forEach(v -> targetMapping.put(v, p));
        }


        ProblemContainerNeighbourhoodAware<S, T> result = new ProblemContainerNeighbourhoodAware<>(sizeToProblem, sourceMapping); //, targetMapping);
        return result;
    }
}