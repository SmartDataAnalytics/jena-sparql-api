package org.aksw.isomorphism;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;

public class StateCombinatoricCallback<A, B, S> {
    protected Stack<Entry<A, B>> stack = new Stack<Entry<A, B>>();

    protected LinkedListNode<A> remainingA;
    protected LinkedListNode<B> remainingB;

    protected BiFunction<A, B, S> computeSolutionContribution;
    protected BinaryOperator<S> solutionCombiner;
    protected Predicate<S> isUnsatisfiable;
    protected BiConsumer<Stack<Entry<A, B>>, S> completeMatch;

    public StateCombinatoricCallback(
            LinkedListNode<A> remainingA,
            LinkedListNode<B> remainingB,
            BiFunction<A, B, S> computeSolutionContribution,
            BinaryOperator<S> solutionCombiner,
            Predicate<S> isUnsatisfiable,
            BiConsumer<Stack<Entry<A, B>>, S> completeMatch)
    {
        super();
        this.remainingA = remainingA;
        this.remainingB = remainingB;
        this.computeSolutionContribution = computeSolutionContribution;
        this.solutionCombiner = solutionCombiner;
        this.isUnsatisfiable = isUnsatisfiable;
        this.completeMatch = completeMatch;
    }


    /**
     * This function will modify the collections.
     * The collections will eventually contain the original items.
     *
     * @param as
     * @param bs
     */
    public static <A, B> void createKPermutationsOfN(Collection<A> as, Collection<B> bs) {
        LinkedListNode<A> nas = LinkedListNode.create(as);
        LinkedListNode<B> nbs = LinkedListNode.create(bs);
        int[] i = new int[]{0};
        StateCombinatoricCallback<A, B, Integer> runner =
                new StateCombinatoricCallback<A, B, Integer>(
                        nas,
                        nbs,
                        (a, b) -> 0,
                        (a, b) -> 0,
                        (s) -> false,
                        (stack, s) -> { ++i[0]; }); //System.out.println("MATCH: " + (++i[0]) + stack));

        runner.nextA(0);
        System.out.println("permutions: " + i[0]);
    }


    // [a b c] [1 2 3 4 5] -> [1, 2, 3], [1, 2, 4], [1, 2, 5], [1, 3, 4], ...
    //Stream<Stack<Entry<A, B>>>
    public void nextA(S baseSolution) {

        // pick
        LinkedListNode<A> curr = remainingA.successor;
        if(!curr.isTail()) {
            LinkedListNode<A> pick = curr;
            A a = pick.data;

            pick.unlink();

            curr = pick.successor;

            // recurse
            nextB(baseSolution, a);

            // restore
            pick.relink();
        } else {
            completeMatch.accept(stack, baseSolution);
        }
    }

    public void nextB(S baseSolution, A a) {
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

                    stack.push(new SimpleEntry<>(a, b));

                    // recurse
                    nextA(combined);

                    stack.pop();
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
    public void nextAWithAllPermutationsOfA(S baseSolution) {

        // pick
        LinkedListNode<A> curr = remainingA.successor;
//        System.out.println("as: " + curr);
        while(!curr.isTail()) {
            LinkedListNode<A> pick = curr;
            A a = pick.data;

            pick.unlink();

            curr = pick.successor;
            //System.out.println("as unlink: " + remainingA + "; " + a);

            // recurse
            nextB(baseSolution, a);

            // restore
            pick.relink();
            //System.out.println("as relink: " + remainingA + "; " + a);
        }

        if(remainingA.successor.isTail()) {
             //completeMatch.accept(stack, baseSolution);
        }
    }

}