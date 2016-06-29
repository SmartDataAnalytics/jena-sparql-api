package org.aksw.isomorphism;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StateCombinatoric<A, B, S> {
    protected Stack<Entry<A, B>> stack = new Stack<Entry<A, B>>();

    protected LinkedListNode<A> remainingA;
    protected LinkedListNode<B> remainingB;

    protected BiFunction<A, B, S> computeSolutionContribution;
    protected BinaryOperator<S> solutionCombiner;
    protected Predicate<S> isUnsatisfiable;
    //protected BiConsumer<Stack<Entry<A, B>>, S> completeMatch;

    public StateCombinatoric(
            //Collection<A> remainingA,
            //Collection<B> remainingB,
            LinkedListNode<A> remainingA,
            LinkedListNode<B> remainingB,
            BiFunction<A, B, S> computeSolutionContribution,
            BinaryOperator<S> solutionCombiner,
            Predicate<S> isUnsatisfiable)
            //BiConsumer<Stack<Entry<A, B>>, S> completeMatch)
    {
        super();
        this.remainingA = remainingA;
        this.remainingB = remainingB;
        this.computeSolutionContribution = computeSolutionContribution;
        this.solutionCombiner = solutionCombiner;
        this.isUnsatisfiable = isUnsatisfiable;
        //this.completeMatch = completeMatch;
    }


    /**
     * This function will modify the collections.
     * The collections will eventually contain the original items.
     *
     * @param as
     * @param bs
     */
    public static <A, B> void doInPlace(Collection<A> as, Collection<B> bs) {
        LinkedListNode<A> nas = LinkedListNode.create(as);
        LinkedListNode<B> nbs = LinkedListNode.create(bs);
        //int[] i = new int[]{0};
        StateCombinatoric<A, B, Integer> runner =
                new StateCombinatoric<A, B, Integer>(
                        nas,
                        nbs,
                        (a, b) -> 0,
                        (a, b) -> 0,
                        (s) -> false);
                        //(stack, s) -> { System.out.println("MATCH: " + (++i[0]) + stack); });

        Stream<Stack<Entry<A, B>>> x = runner.nextA(0);
        com.codepoetics.protonpack.StreamUtils.zipWithIndex(x).forEach(y -> System.out.println(y.getIndex()));
        //x.forEach(y -> System.out.println("will this work? " + y));
    }


    // [a b c] [1 2 3 4 5] -> [1, 2, 3], [1, 2, 4], [1, 2, 5], [1, 3, 4], ...
    //Stream<Stack<Entry<A, B>>>
    public Stream<Stack<Entry<A, B>>> nextA(S baseSolution) {

        Iterator<Stream<Stack<Entry<A, B>>>> it = new Iterator<Stream<Stack<Entry<A, B>>>>() {
            LinkedListNode<A> curr = remainingA.successor;
            boolean next = true;

            @Override
            public boolean hasNext() {
                return next;
            }

            @Override
            public Stream<Stack<Entry<A, B>>> next() {
                next = false;
                Stream<Stack<Entry<A, B>>> r;

               if(!curr.isTail()) {
                    LinkedListNode<A> pick = curr;
                    A a = pick.data;

                    pick.unlink();

                    curr = pick.successor;
                    //System.out.println("as unlink: " + remainingA + "; " + a);

                    // recurse
                    r = nextB(baseSolution, a);
                    r = StreamUtils.appendAction(r, () -> {
                        // restore
                        pick.relink();
                    });
               } else {
                   r = Stream.of(stack);
               }
               return r;
                //System.out.println("picked " + a);
            }

        };

        Iterable<Stream<Stack<Entry<A, B>>>> iterable = () -> it;
        Stream<Stack<Entry<A, B>>> result = StreamSupport.stream(iterable.spliterator(), false).flatMap(x -> x);


//        System.out.println("stack: " + stack);
//        // pick
//        Node<A> curr = remainingA.successor;
////        System.out.println("as: " + curr);
//        if(!curr.isTail()) {
//            Node<A> pick = curr;
//            A a = pick.data;
//
//            pick.unlink();
//
//            curr = pick.successor;
//            //System.out.println("as unlink: " + remainingA + "; " + a);
//
//            // recurse
//            result = nextB(baseSolution, a);
//            //System.out.println("picked " + a);
//
//            // restore
//            pick.relink();
//            //System.out.println("as relink: " + remainingA + "; " + a);
//        } else {
//            //completeMatch.accept(stack, baseSolution);
//            result = Stream.of(stack);
//            //System.out.println(stack);
//        }

        return result;
    }

    public Stream<Stack<Entry<A, B>>> nextB(S baseSolution, A a) {
        Stream<Stack<Entry<A, B>>> result;

        //Node<B> curr = remainingB.successor;
        //System.out.println("bs: " + curr);

        Iterator<Stream<Stack<Entry<A, B>>>> it = new Iterator<Stream<Stack<Entry<A, B>>>>() {
            LinkedListNode<B> curr = remainingB.successor;

            @Override
            public boolean hasNext() {
                boolean r = !curr.isTail();
                return r;
            }

            @Override
            public Stream<Stack<Entry<A, B>>> next() {
                Stream<Stack<Entry<A, B>>> r = null;

//                if(!curr.isTail()) {
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

                        stack.push(new SimpleEntry<>(a, b));
//System.out.println("push: " + stack);
                        // recurse
                        r = nextA(combined);
                        r = StreamUtils.appendAction(r, () -> {
                            stack.pop();
                            // restore
                            pick.relink();

                        });
                    } else {
                        r = Stream.empty();
                    }

                return r;
            }

        };

        Iterable<Stream<Stack<Entry<A, B>>>> iterable = () -> it;
        result = StreamSupport.stream(iterable.spliterator(), false).flatMap(x -> x);

//        while(!curr.isTail()) {
//            Node<B> pick = curr;
//            B b = pick.data;
//
//            pick.unlink();
//            curr = pick.successor;
//
//            S solutionContribution = computeSolutionContribution.apply(a, b);
//            S combined;
//            boolean unsatisfiable;
//            unsatisfiable = isUnsatisfiable.test(solutionContribution);
//            if(!unsatisfiable) {
//                combined = solutionCombiner.apply(baseSolution, solutionContribution);
//                unsatisfiable = isUnsatisfiable.test(combined);
//
//                if(!unsatisfiable) {
//
//                    stack.push(new SimpleEntry<>(a, b));
//
//                    // recurse
//                    nextA(combined);
//
//                    stack.pop();
//                }
//            }
//
//            // restore
//            pick.relink();
//        }

        return result;
    }


    // This is too excessive ;)
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

    public static void main(String[] args) {
        List<String> as = new ArrayList<String>(Arrays.asList("a", "b", "c"));
        List<Integer> bs = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4, 5));
        doInPlace(as, bs);
    }
}