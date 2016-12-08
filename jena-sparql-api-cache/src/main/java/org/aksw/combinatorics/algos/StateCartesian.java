package org.aksw.combinatorics.algos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.aksw.combinatorics.collections.Combination;
import org.aksw.combinatorics.collections.CombinationStack;

import com.codepoetics.protonpack.functions.TriFunction;

/**
 * Cartesian product generation with support for solution computation and early bail out
 * on unsatisfiable solutions
 *
 * @author raven
 *
 * @param <A>
 * @param <B>
 */
public class StateCartesian<A, B, S> {
    /**
     * The purpose of the lookup function is, that given
     * an item of A together with the so-far found solution,
     * retrieve a set of candidates for further processing
     *
     * In the simplest case, the lookup function returns always
     * an iterator over the same collection of B items.
     *
     */
    //protected Iterator<A> itA;
    protected List<A> as;


    // cached size of as
    protected int asSize;

    // TODO We could add a function which allows reordering the items of as
    // after choosing an item b (returned by the lookup function)

    /**
     * Lookup function that for a given item of A, the partial solution (the stack)
     * yields the candidate items of B for the given solution contribution S.
     */
    protected TriFunction<A, S, CombinationStack<A, B, S>, Iterator<B>> lookupB;


    protected BiFunction<A, B, S> computeSolutionContribution;
    protected BinaryOperator<S> solutionCombiner;
    protected Predicate<S> isUnsatisfiable;
    protected Consumer<CombinationStack<A, B, S>> completeMatch;

    public StateCartesian(
            List<A> as,
            TriFunction<A, S, CombinationStack<A, B, S>, Iterator<B>> lookupB,

            BiFunction<A, B, S> computeSolutionContribution,
            BinaryOperator<S> solutionCombiner,
            Predicate<S> isUnsatisfiable,
            Consumer<CombinationStack<A, B, S>> completeMatch)
    {
        super();
        this.as = as;
        this.lookupB = lookupB;
        this.computeSolutionContribution = computeSolutionContribution;
        this.solutionCombiner = solutionCombiner;
        this.isUnsatisfiable = isUnsatisfiable;
        this.completeMatch = completeMatch;

        asSize = as.size();
    }

    public void run(S baseSolution) {
        nextA(baseSolution, 0, null);
    }

    public void nextA(S baseSolution, int ia, CombinationStack<A, B, S> stack) {

        if(ia >= asSize) {
            completeMatch.accept(stack);
        } else {
            A a = as.get(ia);

            Iterator<B> itB = lookupB.apply(a, baseSolution, stack);

            while(itB.hasNext()) {
                B b = itB.next();
                S contribution = computeSolutionContribution.apply(a, b);
                S combination = solutionCombiner.apply(baseSolution, contribution);
                boolean unsatisfiable = isUnsatisfiable.test(combination);

                if(!unsatisfiable) {

                    Combination<A, B, S> c = new Combination<>(a, b, combination);
                    CombinationStack<A, B, S> newStack = new CombinationStack<>(stack, c);

                    nextA(combination, ia + 1, newStack);
                }
            }
        }
    }

    public static void main(String[] args) {
        List<String> as = new ArrayList<String>(Arrays.asList("a", "b", "c"));
        List<Integer> bs = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4, 5));
        createCartesian(as, bs).forEach(item -> {
            System.out.println(item);
        });


    }

    public static <A, B> Stream<CombinationStack<A, B, Void>> createCartesian(
            Collection<A> as,
            Collection<B> bs) {
        Void nil = null;
        Stream<CombinationStack<A, B, Void>> result = createCartesian(as, bs, nil, (k, n) -> nil, (sa, sb) -> nil, (s) -> false);
        return result;
    }

    /**
     * This function will modify the collections.
     * The collections will eventually contain the original items.
     *
     * @param as
     * @param bs
     */
    public static <A, B, S> Stream<CombinationStack<A, B, S>> createCartesian(
            Collection<A> as,
            Collection<B> bs,
            S baseSolution,
            BiFunction<A, B, S> computeSolutionContribution,
            BinaryOperator<S> solutionCombiner,
            Predicate<S> isUnsatisfiable
            ) {
        List<A> las = as instanceof List ? (List<A>)as : new ArrayList<>(as);

//        List<A> las = new ArrayList<>(as);
//
//        // A little trick for the nested stack to hold items of A in the specified order
//        // but probably not really needed; also the alternation of Bs is still reverse order
        //[(a, 1; null), (b, 1; null), (c, 1; null)]
        //[(a, 2; null), (b, 1; null), (c, 1; null)]
//        Collections.reverse(las);


        TriFunction<A, S, CombinationStack<A, B, S>, Iterator<B>> lookupB = (a, s, stack) -> bs.iterator();

        List<CombinationStack<A, B, S>> list = new ArrayList<>();

        StateCartesian<A, B, S> runner =
                new StateCartesian<A, B, S>(
                        las,
                        lookupB,
                        computeSolutionContribution,
                        solutionCombiner,
                        isUnsatisfiable,
                        (stack) -> {
                            list.add(stack);
                        }); //System.out.println("MATCH: " + (++i[0]) + stack));

        runner.run(baseSolution);

        Stream<CombinationStack<A, B, S>> result = list.stream();
        return result;
    }

}
