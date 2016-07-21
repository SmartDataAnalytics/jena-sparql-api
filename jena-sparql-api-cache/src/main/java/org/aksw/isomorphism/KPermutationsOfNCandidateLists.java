package org.aksw.isomorphism;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

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
 * 
 * 
 * 
 * @author raven
 *
 */
public class KPermutationsOfNCandidateLists<A, B, S>
    extends KPermutationsOfNCallbackBase<A, B, S>
{
    /**
     * A multimap that maps each item of A to its candidates in B.
     * Whenever an 'a' is mapped to a 'b', the 'b' is no longer available for
     * further mappings of 'a'. 
     * 
     */
    protected BiHashMultimap<A, B> remaining;
    
    public KPermutationsOfNCandidateLists(List<A> as,
            TriFunction<S, A, B, Stream<S>> solutionCombiner,
            BiHashMultimap<A, B> remaining) {
        super(as, solutionCombiner);
        this.remaining = remaining;
    }
    
    @Override
    public void nextB(int i, S baseSolution, CombinationStack<A, B, S> stack, Consumer<CombinationStack<A, B, S>> completeMatch) {
        if(i < as.size()) {
            A a = as.get(i);
            
            // TODO Maybe with more clever data structures we can get rid of having to create a copy
            Set<B> bs = new HashSet<>(remaining.get(a));

            for(B b : bs) {
                // Pick                
                Set<A> removals = new HashSet<>(remaining.getInverse().get(b));                
                for(A at : removals) {
                    remaining.remove(at, b);
                }                
                
               
                Stream<S> partialSolutions = solutionCombiner.apply(baseSolution, a, b);
                partialSolutions.forEach(partialSolution -> {
                    Combination<A, B, S> c = new Combination<>(a, b, partialSolution);
                    CombinationStack<A, B, S> newStack = new CombinationStack<>(stack, c);

                    // recurse
                    nextB(i + 1, partialSolution, newStack, completeMatch);
                });

                // restore
                for(A at : removals) {
                    remaining.put(at, b);
                }
            }
        } else {
            completeMatch.accept(stack);
        }

    }   
}
