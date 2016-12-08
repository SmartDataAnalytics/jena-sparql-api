package org.aksw.combinatorics.algos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.aksw.combinatorics.collections.Combination;
import org.aksw.combinatorics.collections.CombinationStack;
import org.aksw.commons.collections.lists.LinkedListNode;
import org.aksw.commons.collections.utils.StreamUtils;

import com.google.common.base.Stopwatch;

public class StateCombinatoricStreamSlow<A, B, S> {
    //protected Stack<Entry<A, B>> stack = new Stack<Entry<A, B>>();

    protected LinkedListNode<A> remainingA;
    protected LinkedListNode<B> remainingB;

    protected BiFunction<A, B, S> computeSolutionContribution;
    protected BinaryOperator<S> solutionCombiner;
    protected Predicate<S> isUnsatisfiable;
    //protected BiConsumer<Stack<Entry<A, B>>, S> completeMatch;

    public StateCombinatoricStreamSlow(
            LinkedListNode<A> remainingA,
            LinkedListNode<B> remainingB,
            BiFunction<A, B, S> computeSolutionContribution,
            BinaryOperator<S> solutionCombiner,
            Predicate<S> isUnsatisfiable)
    {
        super();
        this.remainingA = remainingA;
        this.remainingB = remainingB;
        this.computeSolutionContribution = computeSolutionContribution;
        this.solutionCombiner = solutionCombiner;
        this.isUnsatisfiable = isUnsatisfiable;
    }


    public static <A, B> Stream<CombinationStack<A, B, Integer>> createKPermutationsOfN(Collection<A> ks, Collection<B> ns) {
        LinkedListNode<A> nas = LinkedListNode.create(ks);
        LinkedListNode<B> nbs = LinkedListNode.create(ns);
        //int[] i = new int[]{0};
        StateCombinatoricStreamSlow<A, B, Integer> runner =
                new StateCombinatoricStreamSlow<A, B, Integer>(
                        nas,
                        nbs,
                        (a, b) -> 0,
                        (a, b) -> 0,
                        (s) -> false);

        Stream<CombinationStack<A, B, Integer>> result = runner.stream(0);
        return result;
    }

    public Stream<CombinationStack<A, B, S>> stream(S baseSolution) {
        Stream<CombinationStack<A, B, S>> result = streamA(baseSolution, null);
        return result;
    }
    // [a b c] [1 2 3 4 5] -> [1, 2, 3], [1, 2, 4], [1, 2, 5], [1, 3, 4], ...
    //Stream<Stack<Entry<A, B>>>
    public Stream<CombinationStack<A, B, S>> streamA(S baseSolution, CombinationStack<A, B, S> stack) {

        Iterator<Stream<CombinationStack<A, B, S>>> it = new Iterator<Stream<CombinationStack<A, B, S>>>() {
            LinkedListNode<A> curr = remainingA.successor;
            boolean next = true;

            @Override
            public boolean hasNext() {
                return next;
            }

            @Override
            public Stream<CombinationStack<A, B, S>> next() {
                next = false;
                Stream<CombinationStack<A, B, S>> r;

                if(!curr.isTail()) {
                    LinkedListNode<A> pick = curr;
                    A a = pick.data;

                    pick.unlink();

                    curr = pick.successor;
                    //System.out.println("as unlink: " + remainingA + "; " + a);

                    // recurse
                    r = nextB(baseSolution, a, stack);
                    r = StreamUtils.appendAction(r, () -> {
                        // restore
                        pick.relink();
                    });
               } else {
                   //@SuppressWarnings("unchecked")
                   //Stack<Entry<A, B>> copy = (Stack<Entry<A, B>>)stack.clone();
                   //r = Stream.of(new Combination<A, B, S>(copy, baseSolution));
                   r = Stream.of(stack);
               }
               return r;
                //System.out.println("picked " + a);
            }

        };

        Iterable<Stream<CombinationStack<A, B, S>>> iterable = () -> it;
        Stream<CombinationStack<A, B, S>> result = StreamSupport
                .stream(iterable.spliterator(), false)
                .flatMap(x -> x);

        return result;
    }

    public Stream<CombinationStack<A, B, S>> nextB(S baseSolution, A a, CombinationStack<A, B, S> stack) {
        Stream<CombinationStack<A, B, S>> result;

        Iterator<Stream<CombinationStack<A, B, S>>> it = new Iterator<Stream<CombinationStack<A, B, S>>>() {
            LinkedListNode<B> curr = remainingB.successor;

            @Override
            public boolean hasNext() {
                boolean r = !curr.isTail();
                return r;
            }

            @Override
            public Stream<CombinationStack<A, B, S>> next() {
                Stream<CombinationStack<A, B, S>> r = null;

                LinkedListNode<B> pick = curr;
                B b = pick.data;

                pick.unlink();
                curr = pick.successor;

                S solutionContribution = computeSolutionContribution.apply(a, b);
                S combined = null;
                boolean unsatisfiable;

                unsatisfiable = isUnsatisfiable.test(solutionContribution);
                if(!unsatisfiable) {
                    combined = solutionCombiner.apply(baseSolution, solutionContribution);
                    unsatisfiable = isUnsatisfiable.test(combined);
                }

                if(!unsatisfiable) {

                    Combination<A, B, S> c = new Combination<>(a, b, combined);
                    //stack.push(new SimpleEntry<>(a, b));
                    CombinationStack<A, B, S> newStack = new CombinationStack<>(stack, c);
//System.out.println("push: " + stack);
                    // recurse
                    r = streamA(combined, newStack);
                    r = StreamUtils.appendAction(r, () -> {
                        // restore
                        pick.relink();

                    });
                } else {
                    r = Stream.empty();
                }

                return r;
            }
        };

        Iterable<Stream<CombinationStack<A, B, S>>> iterable = () -> it;
        result = StreamSupport.stream(iterable.spliterator(), false).flatMap(x -> x);

        return result;
    }


    public static void main(String[] args) {
//        List<String> as = new ArrayList<String>(Arrays.asList("a", "b", "c", "d", "e", "f", "g"));
//        List<Integer> bs = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));

        List<String> as = new ArrayList<String>(Arrays.asList("a", "b", "c", "d", "e", "f"));
        List<Integer> bs = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4, 5, 6));

//        Collection<String> strs = new ArrayList<String>();


        for(int i = 0; i < 1000; ++i) {
            Stopwatch sw = Stopwatch.createStarted();
            Stream<?> stream = null;
            //stream = StateCombinatoricCallback.createKPermutationsOfN(as, bs);
            //System.out.println("huh");
            stream = createKPermutationsOfN(as, bs);
            //IntStream stream = IntStream.range(0, 181440000);
            //stream = IntStream.range(0, 181440).mapToObj(x -> "mystr" + x);
            long count = stream != null ? stream.count() : 0;
            System.out.println("Time taken for " + count + " items: " + sw.stop().elapsed(TimeUnit.MILLISECONDS));
        }
//        com.codepoetics.protonpack.StreamUtils.zipWithIndex(stream).forEach(
//                y -> System.out.println(y.getIndex() + " - " + y.getValue()));

    }
}