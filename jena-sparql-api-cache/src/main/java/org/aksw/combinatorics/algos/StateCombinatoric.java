package org.aksw.combinatorics.algos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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

import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterators;


public class StateCombinatoric<A, B, S> {
    //protected Stack<Entry<A, B>> stack = new Stack<Entry<A, B>>();

    protected LinkedListNode<A> remainingA;
    protected LinkedListNode<B> remainingB;

    protected BiFunction<A, B, S> computeSolutionContribution;
    protected BinaryOperator<S> solutionCombiner;
    protected Predicate<S> isUnsatisfiable;
    //protected BiConsumer<Stack<Entry<A, B>>, S> completeMatch;

    public StateCombinatoric(
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


    public static <A, B> Stream<CombinationStack<A, B, Void>> createKPermutationsOfN(Collection<A> ks, Collection<B> ns) {
        LinkedListNode<A> nas = LinkedListNode.create(ks);
        LinkedListNode<B> nbs = LinkedListNode.create(ns);

        //int[] i = new int[]{0};
        Void nil = null;
        StateCombinatoric<A, B, Void> runner =
                new StateCombinatoric<>(
                        nas,
                        nbs,
                        (a, b) -> nil,
                        (a, b) -> nil,
                        (s) -> false);

        Iterator<CombinationStack<A, B, Void>> iterator = runner.stream(nil);

        Iterable<CombinationStack<A, B, Void>> iterable = () -> iterator;
        Stream<CombinationStack<A, B, Void>> result = StreamSupport.stream(iterable.spliterator(), false);

        return result;
    }

    public Iterator<CombinationStack<A, B, S>> stream(S baseSolution) {
        Iterator<CombinationStack<A, B, S>> result = streamA(baseSolution, null, () -> {});
        return result;
    }
    // [a b c] [1 2 3 4 5] -> [1, 2, 3], [1, 2, 4], [1, 2, 5], [1, 3, 4], ...
    //Stream<Stack<Entry<A, B>>>
    public Iterator<CombinationStack<A, B, S>> streamA(S baseSolution, CombinationStack<A, B, S> stack, Runnable closeAction) {

        Iterator<Iterator<CombinationStack<A, B, S>>> it = new Iterator<Iterator<CombinationStack<A, B, S>>>() {
            LinkedListNode<A> curr = remainingA.successor;
            boolean next = true;

            @Override
            public boolean hasNext() {
                if(!next) {
                    closeAction.run();
                }
                return next;
            }

            @Override
            public Iterator<CombinationStack<A, B, S>> next() {
                next = false;
                Iterator<CombinationStack<A, B, S>> r;

                if(!curr.isTail()) {
                    LinkedListNode<A> pick = curr;
                    A a = pick.data;

                    pick.unlink();

                    curr = pick.successor;
                    //System.out.println("as unlink: " + remainingA + "; " + a);

                    // recurse
                    r = nextB(baseSolution, a, stack, () -> {
                        // restore
                        pick.relink();
                    });

               } else {
                   //@SuppressWarnings("unchecked")
                   //Stack<Entry<A, B>> copy = (Stack<Entry<A, B>>)stack.clone();
                   //r = Stream.of(new Combination<A, B, S>(copy, baseSolution));
                   r = Collections.singleton(stack).iterator();//Stream.of(stack);
               }
               return r;
                //System.out.println("picked " + a);
            }

        };

        Iterator<CombinationStack<A, B, S>> result = Iterators.concat(it);
        return result;
    }

    public Iterator<CombinationStack<A, B, S>> nextB(S baseSolution, A a, CombinationStack<A, B, S> stack, Runnable closeAction) {
        Iterator<Iterator<CombinationStack<A, B, S>>> it = new Iterator<Iterator<CombinationStack<A, B, S>>>() {
            LinkedListNode<B> curr = remainingB.successor;

            @Override
            public boolean hasNext() {
                boolean r = !curr.isTail();

                if(r == false) {
                    closeAction.run();
                }

                return r;
            }

            @Override
            public Iterator<CombinationStack<A, B, S>> next() {
                Iterator<CombinationStack<A, B, S>> r = null;

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
                    r = streamA(combined, newStack, () -> {
                        // restore
                        pick.relink();
                    });
                } else {
                    r = Collections.emptyIterator();
                }

                return r;
            }
        };

        Iterator<CombinationStack<A, B, S>> result = Iterators.concat(it);
        return result;
    }


    public static void main(String[] args) {
        List<String> as = new ArrayList<String>(Arrays.asList("a", "b", "c", "d", "e", "f", "g"));
        List<Integer> bs = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));

//        List<String> as = new ArrayList<String>(Arrays.asList("a", "b", "c"));
//        List<Integer> bs = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4, 5));

//        Collection<String> strs = new ArrayList<String>();


        for(int i = 0; i < 1; ++i) {
            Stopwatch sw = Stopwatch.createStarted();
            Stream<?> stream = null;
            stream = StateCombinatoricCallback.createKPermutationsOfN2(as, bs);
            //System.out.println("huh");
            //stream = createKPermutationsOfN(as, bs);
            //IntStream stream = IntStream.range(0, 181440000);
            //stream = IntStream.range(0, 181440).mapToObj(x -> "mystr" + x);
            if(true) {
                stream = stream.limit(1);
                com.codepoetics.protonpack.StreamUtils.zipWithIndex(stream).forEach(y -> System.out.println(y.getIndex() + " - " + y.getValue()));
            } else {
                long count = stream != null ? stream.count() : 0;
                System.out.println("Time taken for " + count + " items: " + sw.stop().elapsed(TimeUnit.MILLISECONDS));
            }
        }

    }
}