package org.aksw.combinatorics.algos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.aksw.combinatorics.collections.Combination;
import org.aksw.combinatorics.collections.CombinationStack;
import org.aksw.commons.collections.multimaps.BiHashMultimap;

import com.codepoetics.protonpack.functions.TriFunction;


/**
 * A k permutations of n implementation using candidate mapping lists between
 * items of K and N.
 *
 * This is a more restrictive version of k permutations of n where each
 * k does not map to all n, but only a subset of them n.
 *
 *
 * The solution datatype is Map<ClusterKey, Multimap<A, B>> - i.e. for every cluster the candidate
 * mappings.
 *
 *
 * @author raven
 *
 */
public class KPermutationsOfNCandidateLists<A, B, S>
    //extends KPermutationsOfNCallbackBase<A, B, S>
{
    protected List<A> as;

    /**
     * A multimap that maps each item of A to its candidates in B.
     * Whenever an 'a' is mapped to a 'b', the 'b' is no longer available for
     * further mappings of 'a'.
     *
     */
    protected BiHashMultimap<A, B> remaining;
    protected TriFunction<S, A, B, Stream<S>> solutionCombiner;

    public KPermutationsOfNCandidateLists(List<A> as,
            BiHashMultimap<A, B> remaining,
            TriFunction<S, A, B, Stream<S>> solutionCombiner) {
        //super(as, null); //solutionCombiner);
        this.as = as;
        this.remaining = remaining;
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
            recurse(0, baseSolution, null, completeMatch);
        }
    }

    //@Override
    public void recurse(int i, S baseSolution, CombinationStack<A, B, S> stack, Consumer<CombinationStack<A, B, S>> completeMatch) {
        if(i < as.size()) {
            A a = as.get(i);

            // TODO Maybe with more clever data structures we can get rid of having to create a copy
            List<B> bs = new ArrayList<>(remaining.get(a));

            for(B b : bs) {
                // Now we picked a 'b'

                // Get all as that map to the b and remove them
                //Set<A> affectedAs = remaining.getInverse().get(b);
                // TODO Can we avoid the copy?
                Collection<A> affectedAs = new ArrayList<A>(remaining.getInverse().get(b));

                // Remove the clusterCandidateMapping from the remaining set
                for(A affectedA : affectedAs) {
                    remaining.remove(affectedA, b);
                }

//                Combination<A, B, S> combination = new Combination<>(a, b, partialSolution);
//                CombinationStack<A, B, S> newStack = new CombinationStack<>(stack, combination);
//                nextB(i + 1, baseSolution, newStack, completeMatch);

                Stream<S> partialSolutions = solutionCombiner.apply(baseSolution, a, b);
                partialSolutions.forEach(partialSolution -> {
                    Combination<A, B, S> c = new Combination<>(a, b, partialSolution);
                    CombinationStack<A, B, S> newStack = new CombinationStack<>(stack, c);

                    // recurse
                    recurse(i + 1, partialSolution, newStack, completeMatch);
                });

                // restore
                for(A affectedA : affectedAs) {
                    remaining.put(affectedA, b);
                }
            }
        } else {
            completeMatch.accept(stack);
        }

    }
}
