package org.aksw.isomorphism;

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

import org.aksw.jena_sparql_api.views.NestedStack;

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

    protected TriFunction<A, S, NestedStack<Combination2<A, B, S>>, Iterator<B>> lookupB;


    protected BiFunction<A, B, S> computeSolutionContribution;
    protected BinaryOperator<S> solutionCombiner;
    protected Predicate<S> isUnsatisfiable;
    protected Consumer<NestedStack<Combination2<A, B, S>>> completeMatch;

    public StateCartesian(
            List<A> as,
            TriFunction<A, S, NestedStack<Combination2<A, B, S>>, Iterator<B>> lookupB,

            BiFunction<A, B, S> computeSolutionContribution,
            BinaryOperator<S> solutionCombiner,
            Predicate<S> isUnsatisfiable,
            Consumer<NestedStack<Combination2<A, B, S>>> completeMatch)
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

    public void nextA(S baseSolution, int ia, NestedStack<Combination2<A, B, S>> stack) {

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

                    Combination2<A, B, S> c = new Combination2<>(a, b, combination);
                    NestedStack<Combination2<A, B, S>> newStack = new NestedStack<>(stack, c);

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

    public static <A, B> Stream<NestedStack<Combination2<A, B, Void>>> createCartesian(
            Collection<A> as,
            Collection<B> bs) {
        Void nil = null;
        Stream<NestedStack<Combination2<A, B, Void>>> result = createCartesian(as, bs, nil, (k, n) -> nil, (sa, sb) -> nil, (s) -> false);
        return result;
    }

    /**
     * This function will modify the collections.
     * The collections will eventually contain the original items.
     *
     * @param as
     * @param bs
     */
    public static <A, B, S> Stream<NestedStack<Combination2<A, B, S>>> createCartesian(
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


        TriFunction<A, S, NestedStack<Combination2<A, B, S>>, Iterator<B>> lookupB = (a, s, stack) -> bs.iterator();

        List<NestedStack<Combination2<A, B, S>>> list = new ArrayList<>();

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

        Stream<NestedStack<Combination2<A, B, S>>> result = list.stream();
        return result;
    }

}
