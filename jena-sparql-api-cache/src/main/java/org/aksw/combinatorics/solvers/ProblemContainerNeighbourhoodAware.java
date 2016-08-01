package org.aksw.combinatorics.solvers;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;


/**
 *
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
    //implements ProblemContainer<S>
{
    private static final Logger logger = LoggerFactory.getLogger(ProblemContainerNeighbourhoodAware.class);

    /**
     * The queue of open problems.
     * Referred to as the regularQueue
     *
     */
    protected NavigableMap<? super Comparable<?>, Collection<ProblemNeighborhoodAware<S, T>>> regularQueue;

    /**
     * The refinement queue is a subset of costToProblems.
     * It contains problems which are related (based on their neighbourhood) on already solved ones.
     * Hence, if the cost of the cheapest problem in costToProblems exceeds a certain threshold, problems in
     * the refining problems in the refinementQueue have a chance of yielding lower-cost problems.
     *
     */
    protected NavigableMap<? super Comparable<?>, Collection<ProblemNeighborhoodAware<S, T>>> refinementQueue = new TreeMap<>();

    /**
     * Map from neighborhood item (such as SPARQL variables) to problems which make use of them.
     * This data structure is used to populate the refinementQueue.
     *
     */
    protected Multimap<T, ProblemNeighborhoodAware<S, T>> sourceMapping = HashMultimap.create();

    /**
     * Function to extract neighborhood items from a solution object
     */
    protected Function<? super S, ? extends Collection<T>> getRelatedSources;


    protected BinaryOperator<S> solutionCombiner;
    protected Predicate<S> isUnsatisfiable;


    /**
     * Callback for reporting solutions
     *
     */
    protected Consumer<S> solutionCallback;


    public ProblemContainerNeighbourhoodAware(
            Function<? super S, ? extends Collection<T>> getRelatedSources,
            BinaryOperator<S> solutionCombiner,
            Predicate<S> isUnsatisfiable,
            Consumer<S> solutionCallback) {
        super();
        this.regularQueue = new TreeMap<>();
        this.sourceMapping = HashMultimap.create();
        this.refinementQueue = new TreeMap<>();

        this.getRelatedSources = getRelatedSources;
        this.solutionCombiner = solutionCombiner;
        this.isUnsatisfiable = isUnsatisfiable;
        this.solutionCallback = solutionCallback;
    }


    public void addToRegularQueue(ProblemNeighborhoodAware<S, T> problem) {
        Comparable<?> cost = problem.getEstimatedCost();
        regularQueue.computeIfAbsent(cost, (x) -> new HashSet<>()).add(problem);

        Collection<T> sourceNeigbourhood = problem.getSourceNeighbourhood();
        sourceNeigbourhood.forEach(neighbour -> sourceMapping.put(neighbour, problem));
    }


    // A problem in the refinement queue is removed from the regularQueue
    public void moveFromRegularToRefinementQueue(ProblemNeighborhoodAware<S, T> problem) {
        removeFromRegularQueue(problem);

        Comparable<?> cost = problem.getEstimatedCost();
        refinementQueue.computeIfAbsent(cost, (x) -> new HashSet<>()).add(problem);
    }

    public void removeFromRefinementQueue(ProblemNeighborhoodAware<S, T> problem) {
        Comparable<?> cost = problem.getEstimatedCost();
        remove(refinementQueue, cost, problem);
    }

    public void moveFromRefinementToRegularQueue(ProblemNeighborhoodAware<S, T> problem) {
        Comparable<?> cost = problem.getEstimatedCost();

        remove(refinementQueue, cost, problem);

        addToRegularQueue(problem);
    }

    // Does this take the partialSolution or the solutionContribution???

//    public void index(S solutionContribution) {
//        Collection<T> solutionNeighborhood = getRelatedSources.apply(solutionContribution);
//
//        solutionNeighborhood.forEach(neighbor -> {
//            Collection<ProblemNeighborhoodAware<S, T>> relatedProblems = sourceMapping.get(neighbor);
//
//            relatedProblems.forEach(p -> {
//                removeFromRegularQueue(p);
//            });
//
//
//        });
//
//    }

    public void remove(ProblemNeighborhoodAware<S, T> problem) {
        removeFromRegularQueue(problem);
        removeFromRefinementQueue(problem);
    }

    public void removeFromRegularQueue(ProblemNeighborhoodAware<S, T> problem) {
        Comparable<?> cost = problem.getEstimatedCost();

        // Remove the problem from the refinement queue if it is in it
        //remove(refinementQueue, cost, problem);
        remove(regularQueue, cost, problem);

        // Remove the problem from the neigbourhood index
        Collection<T> sourceNeigborhood = problem.getSourceNeighbourhood();
        sourceNeigborhood.forEach(neighbor -> sourceMapping.remove(neighbor, problem));
    }

    // TODO Move to multimap utils
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

    public static <K, V, W extends Iterable<V>> Entry<K, V> pollFirstEntry(NavigableMap<K, W> map) {
        Entry<K, V> result = null;

        Iterator<Entry<K, W>> itE = map.entrySet().iterator();

        // For robustness, remove entry valueX sets
        while(itE.hasNext()) {
            Entry<K, W> e = itE.next();

            K k = e.getKey();
            Iterable<V> vs = e.getValue();

            Iterator<V> itV = vs.iterator();
            if(itV.hasNext()) {
                V v = itV.next();
                itV.remove();

                if(!itV.hasNext()) {
                    itE.remove();
                }

                result = new SimpleEntry<>(k, v);
                break;
            } else {
                itE.remove();
                continue;
            }
        }

        return result;
    }

    public static boolean isEmpty(Map<?, ? extends Collection<?>> mm) {
        boolean result = true;
        for(Collection<?> c : mm.values()) {
            result = c.isEmpty();
            if(!result) {
                break;
            }
        }
        return result;
    }

    public static int size(Map<?, ? extends Collection<?>> mm) {
        int result = 0;
        for(Collection<?> c : mm.values()) {
            result += c.size();
        }
        return result;
    }

    public static <K, V> Entry<K, V> firstEntry(NavigableMap<K, ? extends Iterable<V>> map) {
        Entry<K, V> result = null;

        Iterator<? extends Entry<K, ? extends Iterable<V>>> itE = map.entrySet().iterator();

        // For robustness, remove entry valueX sets
        while(itE.hasNext()) {
            Entry<K, ? extends Iterable<V>> e = itE.next();

            K k = e.getKey();
            Iterable<V> vs = e.getValue();

            Iterator<V> itV = vs.iterator();
            if(itV.hasNext()) {
                V v = itV.next();

                result = new SimpleEntry<>(k, v);
                break;
            } else {
                continue;
            }
        }

        return result;
    }


    // Refine the current problem in an attempt to make it even cheaper
//    public void processRefinementQueue2(S solution) {
//        Entry<? super Comparable<?>, ProblemNeighborhoodAware<S, T>> currEntry = firstEntry(regularQueue);
//
//        ProblemNeighborhoodAware<S, T> p = currEntry.getValue();
//        remove(p);
//
//        Collection<? extends ProblemNeighborhoodAware<S, T>> newProblems = p.refine(solution);
//        for(ProblemNeighborhoodAware<S, T> newP : newProblems) {
//            addToRegularQueue(newP);
//        }
//    }

    public void processRefinementQueue(S solution) {

        logger.debug("Processing " + size(refinementQueue) + " items in the refinement queue");

        // Simple approach: Always process the whole queue
        while(!refinementQueue.isEmpty()) {
            Entry<? super Comparable<?>, ProblemNeighborhoodAware<S, T>> entry = pollFirstEntry(refinementQueue);

            ProblemNeighborhoodAware<S, T> refinee = entry.getValue();
            Collection<? extends ProblemNeighborhoodAware<S, T>> newProblems = refinee.refine(solution);

            logger.debug("  Refined a problem into " + newProblems.size() + " further problems");

            for(ProblemNeighborhoodAware<S, T> newProblem : newProblems) {
                addToRegularQueue(newProblem);
            }
        }


        //
//      long refinementConsiderationThreshold = 2;
//      long refinementAcceptanceThreshold = 1; // currEntry.cost
//
//      if(currEntry != null) {
//          long cost = currEntry.getKey();
//
//          // refine in the hope of
//          if(cost > refinementConsiderationThreshold) {
//              Collection<ProblemNeighborhoodAware<S, T>> problems = refinementEntry.getValue();
//
//              for(ProblemNeighborhoodAware<S, T> problem : problems) {
//                  problem.refine(baseSolution);
//              }
//
//
//          }
//
//      }

    }


    /**
     * Pick the estimated cheapest problem,
     * return a a problem collection with that generator removed
     *
     * @return
     */
    public void run(S baseSolution) {
        // If there are no open problems, we found a complete solution
        if(isEmpty(regularQueue)) {
            solutionCallback.accept(baseSolution);
        } else {
            logger.debug("Next Iteration with regular queue size: " + size(regularQueue) + " and base solution " + baseSolution);

            // TODO We need to consider the costs of whether processing the refinement queue makes sense
            // As long as there are cheap problems in the regular queue, it does not make sense to refine
            // Also, if a cheap problem mas an expensive related problem, the expensive problem's refinement should be
            // delayed for as long as possible
            //processRefinementQueue2(baseSolution);

            Entry<? super Comparable<?>, ProblemNeighborhoodAware<S, T>> firstEntry = firstEntry(regularQueue);

            // NOTE The refinement queue could also be ordered by the number of neighbourhood items
            //Entry<? super Comparable<?>, ProblemNeighborhoodAware<S, T>> refinementEntry = firstEntry(refinementQueue); //.firstEntry();
            //Entry<? super Comparable<?>, ProblemNeighborhoodAware<S, T>> currEntry = pollFirstEntry(regularQueue);

            //logger.debug("  After pick: Regular queue size: " + size(regularQueue));

            // if the cost is high, consider refining (some of) the problems in the refinement queue
            // add the refined problems to the sizeToProblem queue

            Object firstCost = firstEntry.getKey();
            ProblemNeighborhoodAware<S, T> firstProblem = firstEntry.getValue();

            remove(firstProblem);

            Collection<? extends ProblemNeighborhoodAware<S, T>> newProblems = firstProblem.refine(baseSolution);
            for(ProblemNeighborhoodAware<S, T> newP : newProblems) {
                addToRegularQueue(newP);
            }

            logger.debug("  First problem in regular queue with cost " + firstCost + " was refined into " + newProblems.size() + " sub Problems; problem was: "+ firstProblem);


            Entry<? super Comparable<?>, ProblemNeighborhoodAware<S, T>> pickedEntry = firstEntry(regularQueue);
            Object pickedCost = pickedEntry.getKey();
            ProblemNeighborhoodAware<S, T> pickedProblem = pickedEntry.getValue();

            logger.debug("  Picked problem with cost " + pickedCost + "; " + pickedProblem);


            // Remove the pick from the datastructures...

            // Remove the picked problem completely
            remove(pickedProblem);
            logger.debug("  Picked problem " + pickedProblem + " with cost " + pickedCost + "; regular queue size is now: " + size(regularQueue));

//                remove(refinementQueue, pickedKey, pickedProblem);
//                remove(costToProblems, pickedKey, pickedProblem);

            // Remove the problem from the neigbourhood index
//            Collection<T> sourceNeigbourhood = pickedProblem.getSourceNeighbourhood();
//            sourceNeigbourhood.forEach(neighbour -> sourceMapping.remove(neighbour, pickedProblem));

            // recurse
            Stream<S> tmp = pickedProblem.generateSolutions();
            logger.debug("  Got " + tmp.count() + " solution candidates with " + pickedProblem);

            Stream<S> solutions = pickedProblem.generateSolutions();

            solutions.forEach(solutionContribution -> {
                
                S combinedSolution = null;
                boolean unsatisfiable = isUnsatisfiable.test(solutionContribution);
                if(!unsatisfiable) {
                    combinedSolution = solutionCombiner.apply(baseSolution, solutionContribution);
                    unsatisfiable = isUnsatisfiable.test(combinedSolution);
                }

                if(!unsatisfiable) {
                    logger.debug("    Satisfiable solution contribution: " + solutionContribution + "; regular queue size now: " + size(regularQueue));

//                    Collection<T> rs = getRelatedSources.apply(solutionContribution);

//                    if(false) {
//                    // Get the related problems and add them to the refinement queue
//                    Set<ProblemNeighborhoodAware<S, T>> neighbors = rs
//                            .stream()
//                            .flatMap(s -> sourceMapping.get(s).stream())
//                            .collect(Collectors.toSet());
//
//                    for(ProblemNeighborhoodAware<S, T> neighbor : neighbors) {
//                        moveFromRegularToRefinementQueue(neighbor);
//                    }
//                    }

                    // Recurse
                    run(combinedSolution);

                    // Restore state (for processing the next solution)
//                    if(false) {
//                    for(ProblemNeighborhoodAware<S, T> neighbor : neighbors) {
//                        moveFromRefinementToRegularQueue(neighbor);
//                    }
//                    }
                } else {
                    logger.debug("    Skipping unatisfiable solution contribution: " + solutionContribution + "; regular queue size now: " + size(regularQueue));
                }
            });

            addToRegularQueue(pickedProblem);

            for(ProblemNeighborhoodAware<S, T> newP : newProblems) {
                removeFromRegularQueue(newP);
            }
            addToRegularQueue(firstProblem);

        }
    }
//
//    /**
//     * Partition the content of the collection in regard to a partial solution.
//     * This operation may return 'this'
//     *
//     * @param partialSolution
//     */
//    public ProblemContainerNeighbourhoodAware<S, T> refine(S partialSolution) {
//        Collection<Problem<S>> tmp = costToProblems.values().stream()
//            .flatMap(x -> x.stream())
//            .map(problem -> problem.refine(partialSolution))
//            .flatMap(x -> x.stream())
//            .collect(Collectors.toList());
//
//        NavigableMap<Long, Collection<Problem<S>>> sizeToProblem = IsoUtils.indexSolutionGenerators(tmp);
//        ProblemContainerNeighbourhoodAware<S, T> result = null; //ew ProblemContainerNeighbourhoodAware<S, T>(sizeToProblem);
//
//        return result;
//    }

    @SafeVarargs
    public static <S, T> void solve(
            S baseSolution,
            Function<? super S, ? extends Collection<T>> getRelatedSources,
            BinaryOperator<S> solutionCombiner,
            Predicate<S> isUnsatisfiable,
            Consumer<S> solutionCallback,
            ProblemNeighborhoodAware<S, T> ... problems) {
        Collection<ProblemNeighborhoodAware<S, T>> tmp = Arrays.asList(problems);
        solve(tmp, baseSolution, getRelatedSources, solutionCombiner, isUnsatisfiable, solutionCallback);
    }

    public static <S, T> Stream<S> solve(
            Collection<ProblemNeighborhoodAware<S, T>> problems,
            S baseSolution,
            Function<? super S, ? extends Collection<T>> getRelatedSources,
            BinaryOperator<S> solutionCombiner,
            Predicate<S> isUnsatisfiable) {
        List<S> tmp = new ArrayList<S>();
        
        solve(problems,
                baseSolution,
                getRelatedSources,
                solutionCombiner,
                isUnsatisfiable,
                tmp::add);
        
        Stream<S> result = tmp.stream();
        return result;        
    }

    
    public static <S, T> void solve(
            Collection<ProblemNeighborhoodAware<S, T>> problems,
            S baseSolution,
            Function<? super S, ? extends Collection<T>> getRelatedSources,
            BinaryOperator<S> solutionCombiner,
            Predicate<S> isUnsatisfiable,
            Consumer<S> solutionCallback) {

        ProblemContainerNeighbourhoodAware<S, T> result = new ProblemContainerNeighbourhoodAware<>(
                getRelatedSources,
                solutionCombiner,
                isUnsatisfiable,
                solutionCallback
                );

        for(ProblemNeighborhoodAware<S, T> problem : problems) {
            result.addToRegularQueue(problem);
        }

        result.run(baseSolution);

        //return result;
    }
}