package org.aksw.isomorphism;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.jena.ext.com.google.common.collect.Iterables;

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
public class StateCombinatoricCallback<A, B, S> {
    //protected LinkedListNode<A> remainingA;
    protected List<A> as;
    protected LinkedListNode<B> remainingB;

    /**
     * Function which takes the base solution, a and b and returns a new solution
     *
     */
    protected TriFunction<S, A, B, Stream<S>> solutionCombiner;


    /**
     * Callback for complete solutions
     *
     */
    protected Consumer<CombinationStack<A, B, S>> completeMatch;


    public StateCombinatoricCallback(
//            LinkedListNode<A> remainingA,
            List<A> as,
            LinkedListNode<B> remainingB,
            TriFunction<S, A, B, Stream<S>> solutionCombiner,
            Consumer<CombinationStack<A, B, S>> completeMatch)
    {
        super();
//        this.remainingA = remainingA;
        this.as = as;
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
//        LinkedListNode<A> nas = LinkedListNode.create(as);
        List<A> xas = as instanceof List ? (List<A>)as : new ArrayList<>(as);
        LinkedListNode<B> nbs = LinkedListNode.create(bs);

        List<CombinationStack<A, B, S>> list = new ArrayList<>();

        StateCombinatoricCallback<A, B, S> runner =
                new StateCombinatoricCallback<A, B, S>(
                        xas,
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
        boolean isEmpty = as.isEmpty(); //remainingA.successor.isTail();
        if(!isEmpty) {
            nextB(0, baseSolution, null);
        }
    }

    // [a b c] [1 2 3 4 5] -> [1, 2, 3], [1, 2, 4], [1, 2, 5], [1, 3, 4], ...
//    public void nextA(S baseSolution, CombinationStack<A, B, S> stack) {
//
//        // pick
//        LinkedListNode<A> curr = remainingA.successor;
//        if(!curr.isTail()) {
//            LinkedListNode<A> pick = curr;
//            A a = pick.data;
//
//            pick.unlink();
//
//            curr = pick.successor;
//
//            // recurse
//            nextB(i + 1, baseSolution, a, stack);
//
//            // restore
//            pick.relink();
//        } else {
//            completeMatch.accept(stack);
//        }
//    }

    public void nextB(int i, S baseSolution, CombinationStack<A, B, S> stack) {
        if(i < as.size()) {
            A a = as.get(i);
            LinkedListNode<B> curr = remainingB.successor;

            while(!curr.isTail()) {
                LinkedListNode<B> pick = curr;
                B b = pick.data;

                pick.unlink();
                curr = pick.successor;

                Stream<S> partialSolutions = solutionCombiner.apply(baseSolution, a, b);
                partialSolutions.forEach(partialSolution -> {
                    Combination<A, B, S> c = new Combination<>(a, b, partialSolution);
                    CombinationStack<A, B, S> newStack = new CombinationStack<>(stack, c);

                    // recurse
                    nextB(i + 1, partialSolution, newStack);
                });

                // restore
                pick.relink();
            }
        } else {
            completeMatch.accept(stack);
        }

    }

    public Stream<CombinationStack<A, B, S>> stream(S baseSolution) {
        Stream<CombinationStack<A, B, S>> result = as.isEmpty()
                ? Stream.empty()
                : stream(0, baseSolution, null);

        return result;
    }

    public static <A, B> Stream<CombinationStack<A, B, Void>> createKPermutationsOfN2(
            Collection<A> as,
            Collection<B> bs) {
        Void nil = null;
        Stream<CombinationStack<A, B, Void>> result = createKPermutationsOfN2(as, bs, nil, (s, a, b) -> Stream.of(nil));
        return result;
    }

    public static <A, B, S> Stream<CombinationStack<A, B, S>> createKPermutationsOfN2(
            Collection<A> as,
            Collection<B> bs,
            S baseSolution,
            TriFunction<S, A, B, Stream<S>> solutionCombiner
            ) {
        List<A> xas = as instanceof List ? (List<A>)as : new ArrayList<>(as);
        LinkedListNode<B> nbs = LinkedListNode.create(bs);

        List<CombinationStack<A, B, S>> list = new ArrayList<>();

        StateCombinatoricCallback<A, B, S> runner =
                new StateCombinatoricCallback<A, B, S>(
                        xas,
                        nbs,
                        solutionCombiner,
                        (stack) -> {
                            list.add(stack);
                        });

        Stream<CombinationStack<A, B, S>> result = runner.stream(baseSolution);
        return result;
    }

    public Stream<CombinationStack<A, B, S>> stream(int i, S baseSolution, CombinationStack<A, B, S> stack) {


        Stream<CombinationStack<A, B, S>> result;

        if(i < as.size()) {
            A a = as.get(i);

            Iterable<LinkedListNode<B>> curr = () -> remainingB.metaIterator();

            result = StreamSupport.stream(curr.spliterator(), false)
                .flatMap(pick -> {

                    B b = pick.data;
                    System.out.println(i + ": " + a + " - " + b);
                    pick.unlink();

                    Stream<S> partialSolutions = solutionCombiner.apply(baseSolution, a, b);
                    Stream<CombinationStack<A, B, S>> s = partialSolutions.flatMap(partialSolution -> {
                        Combination<A, B, S> c = new Combination<>(a, b, partialSolution);
                        CombinationStack<A, B, S> newStack = new CombinationStack<>(stack, c);

                        // recurse
                        Stream<CombinationStack<A, B, S>> t = stream(i + 1, partialSolution, newStack);

                        t = StreamUtils.appendAction(t, () -> pick.relink());

                        return t;
                    });
                return s;
            });
        } else {
            System.out.println("Done - with stack " + stack);
            result = Stream.of(stack);
        }

//        closeAction.run();

        return result;
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