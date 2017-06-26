package org.aksw.combinatorics.algos;

import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * An aggregate is a specification of how to compute overall values from
 * contributions.  
 * 
 * @author raven
 *
 * @param <A>
 * @param <B>
 * @param <C>
 * @param <S>
 */
public class Aggregate<A, B, C, S> {
    protected Supplier<S> initialSolution;
    protected BiFunction<A, B, C> pairContribution;
    // The runnable is an undo function - only the *first* argument may be modified
    // Undo is only expected to work if no modifications happen to the first argument after addContribution
    // Note: that the non-undoable contribution addition functions can be made undoable by making copies
    // Hence, we can provide a utility method that makes them undoable
    //protected BiFunction<S, C, S> copyingAddContribution;
    
    //protected BiConsumer<S, C> nonUndoableInPlaceAddContribution;
    protected BiFunction<S, C, ? extends Entry<S, Runnable>> undoableInPlaceAddContribution;
    
    
    protected BiFunction<S, S, ? extends Entry<S, Runnable>> undoableInPlaceMergeSolutions;
    protected Predicate<S> isAcceptable;
    protected Function<S, S> cloneSolution; // May make a copy of the solution
    
    public Aggregate(
            Supplier<S> initialSolution,
            BiFunction<A, B, C> pairContribution,
            BiFunction<S, C, ? extends Entry<S, Runnable>> addContribution,
            BiFunction<S, S, ? extends Entry<S, Runnable>> mergeSolutions,
            Predicate<S> isAcceptable,
            Function<S, S> postProcessSolution) {
        super();
        this.initialSolution = initialSolution;
        this.pairContribution = pairContribution;
        this.undoableInPlaceAddContribution = addContribution;
        this.undoableInPlaceMergeSolutions = mergeSolutions;
        this.isAcceptable = isAcceptable;
        this.cloneSolution = postProcessSolution;
    }

    public Supplier<S> getInitialSolution() {
        return initialSolution;
    }

    public BiFunction<A, B, C> getPairContribution() {
        return pairContribution;
    }

    public BiFunction<S, C, ? extends Entry<S, Runnable>> getAddContribution() {
        return undoableInPlaceAddContribution;
    }

    public BiFunction<S, S, ? extends Entry<S, Runnable>> getMergeSolutions() {
        return undoableInPlaceMergeSolutions;
    }

    public Predicate<S> getIsAcceptable() {
        return isAcceptable;
    }

    public Function<S, S> getPostProcessSolution() {
        return cloneSolution;
    }
}
