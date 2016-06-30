package org.aksw.isomorphism;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.codepoetics.protonpack.functions.TriFunction;

/**
 * kPermutationsOfN implementation with support for solution computation and early bailout should a solution
 * turn out to be unsatisfiable.
 * At the core, this implementation notifies clients about results via a callback during a recursion.
 *
 * A static utility method is available which collects results into a list and returns a stream.
 * Hence, all valid permutations will be computed regardless of the number of items consumed from the stream.
 * However, as this approach is about 5-10 times faster than the recursive stream solution,
 * this approach is recommended.
 *
 *
 * @author raven
 *
 * @param <A>
 * @param <B>
 * @param <S>
 */
public class StateCombinatoricCallbackPair<A, B, S> {
    protected LinkedListNode<A> remainingA;
    protected LinkedListNode<B> remainingB;

    /**
     * Function which takes the base solution, a and b and returns a new solution
     *
     */
    protected TriFunction<S, A, B, Stream<S>> solutionCombiner;

    protected BiPredicate<A, B> isNonMatch; //(A a, B b);

    /**
     * Callback for complete solutions
     *
     */
    protected Consumer<CombinationStack<A, B, S>> completeMatch;

    

    public StateCombinatoricCallbackPair(
            LinkedListNode<A> remainingA,
            LinkedListNode<B> remainingB,
            TriFunction<S, A, B, Stream<S>> solutionCombiner,
            Consumer<CombinationStack<A, B, S>> completeMatch)
    {
        super();
        this.remainingA = remainingA;
        this.remainingB = remainingB;
        this.solutionCombiner = solutionCombiner;
        this.completeMatch = completeMatch;
    }


    public static <A, B> Stream<CombinationStack<A, B, Void>> createKPermutationsOfN(
            Collection<A> as,
            Collection<B> bs) {
        Void nil = null;
        Stream<CombinationStack<A, B, Void>> result = createKPermutationsOfN(as, bs, nil, (s, a, b) -> Stream.of(nil));
        return result;
    }

    public static <A, B, S> Stream<CombinationStack<A, B, S>> createKPermutationsOfN(
            Collection<A> as,
            Collection<B> bs,
            S baseSolution,
            TriFunction<A, B, S, S> pairBasedSolver,
            BinaryOperator<S> solutionCombiner,
            Predicate<S> isUnsatisfiable
            ) {
        LinkedListNode<A> nas = LinkedListNode.create(as);
        LinkedListNode<B> nbs = LinkedListNode.create(bs);

        //Map<A, Collection<B>> aToNonMatches = new HashMap<>();
        Map<A, LinkedListNode<B>> aToMatches = new HashMap<>();
        
        //Map<Entry<A, B>, S> pairSolutionCache = new HashMap<>();
        
        boolean usePairCache = true;
        
        //Map<Entry<A, Map<B, S>> pairSolutionCache = null;
        Map<Entry<A, B>, S> pairSolutionCache = new HashMap<>();
        if(usePairCache) {
            pairSolutionCache = new HashMap<>();
        }
        
        
        // Create pair wise solutions
        for(A a : nas) {
            for(B b: nbs) {
                S pairSolution = pairBasedSolver.apply(a, b, baseSolution);
                boolean unsatisfiable = isUnsatisfiable.test(pairSolution);
                
                if(unsatisfiable) {
                    
                } else {
                    if(usePairCache) {
                        pairSolutionCache.put(new SimpleEntry<>(a, b), pairSolution);
                    }
                }
            }
        }
        
        
        
        
        List<CombinationStack<A, B, S>> list = new ArrayList<>();

        StateCombinatoricCallback<A, B, S> runner = null;
                
//                new StateCombinatoricCallback<A, B, S>(
//                        nas,
//                        nbs,
//                        solutionCombiner,
//                        (stack) -> {
//                            list.add(stack);
//                        });

        runner.run(baseSolution);

        Stream<CombinationStack<A, B, S>> result = list.stream();
        return result;
    }
    
    /**
     * This function will modify the collections.
     * The collections will eventually contain the original items.
     *
     * @param as
     * @param bs
     */
    public static <A, B, S> Stream<CombinationStack<A, B, S>> createKPermutationsOfN(
            Collection<A> as,
            Collection<B> bs,
            S baseSolution,
            TriFunction<S, A, B, Stream<S>> solutionCombiner
            ) {
        LinkedListNode<A> nas = LinkedListNode.create(as);
        LinkedListNode<B> nbs = LinkedListNode.create(bs);

        List<CombinationStack<A, B, S>> list = new ArrayList<>();

        StateCombinatoricCallback<A, B, S> runner =
                new StateCombinatoricCallback<A, B, S>(
                        nas,
                        nbs,
                        solutionCombiner,
                        (stack) -> {
                            list.add(stack);
                        });

        runner.run(baseSolution);

        Stream<CombinationStack<A, B, S>> result = list.stream();
        return result;
    }

    public void run(S baseSolution) {
        boolean isEmpty = remainingA.successor.isTail();
        if(!isEmpty) {
            nextA(baseSolution, null);
        }
    }

    // [a b c] [1 2 3 4 5] -> [1, 2, 3], [1, 2, 4], [1, 2, 5], [1, 3, 4], ...
    public void nextA(S baseSolution, CombinationStack<A, B, S> stack) {

        // pick
        LinkedListNode<A> curr = remainingA.successor;
        if(!curr.isTail()) {
            LinkedListNode<A> pick = curr;
            A a = pick.data;

            pick.unlink();

            curr = pick.successor;

            // recurse
            nextB(baseSolution, a, stack);

            // restore
            pick.relink();
        } else {
            completeMatch.accept(stack);
        }
    }

    public void nextB(S baseSolution, A a, CombinationStack<A, B, S> stack) {
        LinkedListNode<B> curr = remainingB.successor;

        while(!curr.isTail()) {
            
            
            // Skip over non-matching items
            LinkedListNode<B> pick;
            B tmpB = null;
            do {
                pick = curr;
                tmpB = pick.data;                
            } while(isNonMatch.test(a, tmpB) && !curr.isTail());
            
            // Assigment needed because of otherwise 'effectively final' error
            B b = tmpB;
            
            if(!curr.isTail()) {
    
                pick.unlink();
                curr = pick.successor;
    
                Stream<S> partialSolutions = solutionCombiner.apply(baseSolution, a, b);
                partialSolutions.forEach(partialSolution -> {
                    Combination<A, B, S> c = new Combination<>(a, b, partialSolution);
                    CombinationStack<A, B, S> newStack = new CombinationStack<>(stack, c);
    
                    // recurse
                    nextA(partialSolution, newStack);
                });
    
                // restore
                pick.relink();
            }
        }
    }

    public static void main(String[] args) {
        List<String> as = new ArrayList<String>(Arrays.asList("a", "b", "c"));
        List<Integer> bs = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4, 5));
        createKPermutationsOfN(as, bs);
    }

    // This is too excessive - kept for reference;)
//    public void nextAWithAllPermutationsOfA(S baseSolution) {
//
//        // pick
//        LinkedListNode<A> curr = remainingA.successor;
////        System.out.println("as: " + curr);
//        while(!curr.isTail()) {
//            LinkedListNode<A> pick = curr;
//            A a = pick.data;
//
//            pick.unlink();
//
//            curr = pick.successor;
//            //System.out.println("as unlink: " + remainingA + "; " + a);
//
//            // recurse
//            nextB(baseSolution, a, stack);
//
//            // restore
//            pick.relink();
//            //System.out.println("as relink: " + remainingA + "; " + a);
//        }
//
//        if(remainingA.successor.isTail()) {
//             //completeMatch.accept(stack, baseSolution);
//        }
//    }

}