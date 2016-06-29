package org.aksw.isomorphism;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
public class StateCombinatoricCallback<A, B, S> {
    //protected Stack<Entry<A, B>> stack = new Stack<Entry<A, B>>();

    protected LinkedListNode<A> remainingA;
    protected LinkedListNode<B> remainingB;

    protected BiFunction<A, B, S> computeSolutionContribution;
    protected BinaryOperator<S> solutionCombiner;
    protected Predicate<S> isUnsatisfiable;
    protected Consumer<CombinationStack<A, B, S>> completeMatch;

    public StateCombinatoricCallback(
            LinkedListNode<A> remainingA,
            LinkedListNode<B> remainingB,
            BiFunction<A, B, S> computeSolutionContribution,
            BinaryOperator<S> solutionCombiner,
            Predicate<S> isUnsatisfiable,
            Consumer<CombinationStack<A, B, S>> completeMatch)
    {
        super();
        this.remainingA = remainingA;
        this.remainingB = remainingB;
        this.computeSolutionContribution = computeSolutionContribution;
        this.solutionCombiner = solutionCombiner;
        this.isUnsatisfiable = isUnsatisfiable;
        this.completeMatch = completeMatch;
    }


    public static <A, B> Stream<CombinationStack<A, B, Void>> createKPermutationsOfN(
            Collection<A> as,
            Collection<B> bs) {
        Void nil = null;
        Stream<CombinationStack<A, B, Void>> result = createKPermutationsOfN(as, bs, nil, (k, n) -> nil, (sa, sb) -> nil, (s) -> false);
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
            BiFunction<A, B, S> computeSolutionContribution,
            BinaryOperator<S> solutionCombiner,
            Predicate<S> isUnsatisfiable
            ) {
        LinkedListNode<A> nas = LinkedListNode.create(as);
        LinkedListNode<B> nbs = LinkedListNode.create(bs);
        //int[] i = new int[]{0};

        List<CombinationStack<A, B, S>> list = new ArrayList<>();

        StateCombinatoricCallback<A, B, S> runner =
                new StateCombinatoricCallback<A, B, S>(
                        nas,
                        nbs,
                        computeSolutionContribution,
                        solutionCombiner,
                        isUnsatisfiable,
                        (stack) -> {
//                            @SuppressWarnings("unchecked")
//                            Stack<Entry<A, B>> clone = (Stack<Entry<A, B>>)stack.clone();
//                            CombinationStack<A, B, S> c = new CombinationStack<>(stack, s);
                            list.add(stack);
                        }); //System.out.println("MATCH: " + (++i[0]) + stack));

        runner.run(baseSolution);

        Stream<CombinationStack<A, B, S>> result = list.stream();
        return result;
    }

    public void run(S baseSolution) {
        nextA(baseSolution, null);
    }

    // [a b c] [1 2 3 4 5] -> [1, 2, 3], [1, 2, 4], [1, 2, 5], [1, 3, 4], ...
    //Stream<Stack<Entry<A, B>>>
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
            LinkedListNode<B> pick = curr;
            B b = pick.data;

            pick.unlink();
            curr = pick.successor;

            S solutionContribution = computeSolutionContribution.apply(a, b);
            S combined;
            boolean unsatisfiable;
            unsatisfiable = isUnsatisfiable.test(solutionContribution);
            if(!unsatisfiable) {
                combined = solutionCombiner.apply(baseSolution, solutionContribution);
                unsatisfiable = isUnsatisfiable.test(combined);

                if(!unsatisfiable) {

                    Combination<A, B, S> c = new Combination<>(a, b, combined);
                    CombinationStack<A, B, S> newStack = new CombinationStack<>(stack, c);
                    //stack.push(new SimpleEntry<>(a, b));

                    // recurse
                    nextA(combined, newStack);

                    //stack.pop();
                }
            }

            // restore
            pick.relink();
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