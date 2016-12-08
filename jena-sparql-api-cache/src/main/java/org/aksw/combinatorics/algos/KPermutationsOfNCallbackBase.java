package org.aksw.combinatorics.algos;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.aksw.combinatorics.collections.CombinationStack;

import com.codepoetics.protonpack.functions.TriFunction;

public abstract class KPermutationsOfNCallbackBase<A, B, S> {
    protected List<? extends A> as;
    
    /**
     * Function which takes the base solution, a and b and returns a new solution
     *
     */
    protected TriFunction<S, A, B, Stream<S>> solutionCombiner;


    /**
     * Callback for complete solutions
     *
     */

    public KPermutationsOfNCallbackBase(List<? extends A> as,
            TriFunction<S, A, B, Stream<S>> solutionCombiner) {
        super();
        this.as = as;
        this.solutionCombiner = solutionCombiner;
    }

    public Stream<CombinationStack<A, B, S>> stream(S baseSolution) {
        List<CombinationStack<A, B, S>> list = new ArrayList<>();
        
        run(baseSolution, (stack) -> list.add(stack));

        Stream<CombinationStack<A, B, S>> result = list.stream();
        return result;

    }

    public void run(S baseSolution, Consumer<CombinationStack<A, B, S>> completeMatch) {
        boolean isEmpty = as.isEmpty(); //remainingA.successor.isTail();
        if(!isEmpty) {
            nextB(0, baseSolution, null, completeMatch);
        }
    }
        
    public abstract void nextB(int i, S baseSolution, CombinationStack<A, B, S> stack, Consumer<CombinationStack<A, B, S>> completeMatch);

}
