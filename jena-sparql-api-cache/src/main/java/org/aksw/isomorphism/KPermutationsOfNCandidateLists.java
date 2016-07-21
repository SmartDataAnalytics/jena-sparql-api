package org.aksw.isomorphism;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.aksw.commons.collections.multimaps.BiHashMultimap;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;


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
    
    //protected Map<B, BiHashMultimap<A, B>> clustersToRemaining;
    
    protected Function<B, S> bToClusterKey;
    protected Function<S, ? extends Collection<B>> clusterKeyToBs;
    
    
    public KPermutationsOfNCandidateLists(List<A> as,
            //TriFunction<S, A, B, Stream<S>> solutionCombiner,
            BiHashMultimap<A, B> remaining,
            Function<B, S> bToClusterKey,
            Function<S, ? extends Collection<B>> clusterKeyToBs

        ) {
        //super(as, null); //solutionCombiner);
        this.as = as;
        this.remaining = remaining;
        this.bToClusterKey = bToClusterKey;
        this.clusterKeyToBs = clusterKeyToBs;
    }
    
    

    public Stream<ClusterStack<A, B, S>> stream(S baseSolution) {
        List<ClusterStack<A, B, S>> list = new ArrayList<>();
        
        run(baseSolution, (stack) -> list.add(stack));

        Stream<ClusterStack<A, B, S>> result = list.stream();
        return result;

    }

    public void run(S baseSolution, Consumer<ClusterStack<A, B, S>> completeMatch) {
        boolean isEmpty = as.isEmpty(); //remainingA.successor.isTail();
        if(!isEmpty) {
            nextB(0, baseSolution, null, completeMatch);
        }
    }
    
    //@Override
    public void nextB(int i, S baseSolution, ClusterStack<A, B, S> stack, Consumer<ClusterStack<A, B, S>> completeMatch) {
        if(i < as.size()) {
            A a = as.get(i);
            
            // TODO Maybe with more clever data structures we can get rid of having to create a copy
            List<B> bs = new ArrayList<>(remaining.get(a));

            for(B b : bs) {
                // Now we picked a 'b'
                
                
                // Get the clusterKey for b
                // (for tree-children that would usually be their parent)
                S clusterKey = bToClusterKey.apply(b);
                
                // Get all b's that are valid within the cluster
                // (Note: This assumes that this set is known beforehand) 
                // (for a tree, that would be the children of the parent)
                Collection<B> candidateBsInCluster = clusterKeyToBs.apply(clusterKey);
                
                // Get all a's in relation to the candidate b's
                //List<A> affectedAs = new ArrayList<>();
                //Map<B, List<A>> bToAffectedAs;
                //List<A> affectedAs = new ArrayList<>();
                Multimap<A, B> clusterCandidateMapping = HashMultimap.create();
                for(B candidateB : candidateBsInCluster) {
                    Set<A> affectedAs = remaining.getInverse().get(candidateB);

                    for(A affectedA : affectedAs) {
                        clusterCandidateMapping.put(affectedA, candidateB);
                    }
                }
                
                // Remove the clusterCandidateMapping from the remaining set
                for(Entry<A, B> e : clusterCandidateMapping.entries()) {
                    remaining.remove(e.getKey(), e.getValue());
                }
               
                BiHashMultimap<A, B> mm = KPermutationsOfNUtils.create(clusterCandidateMapping);
                boolean abort = false;
                while(abort == false) {

                    boolean change = false;
                    for(Entry<A, Collection<B>> e : mm.asMap().entrySet()) {
                        A keepA = e.getKey();
                        if(e.getValue().size() == 1) {
                            B removeB = e.getValue().iterator().next();

                            Set<A> removeAs = mm.getInverse().get(removeB);
                            for(A removeA : removeAs) {
                                if(removeA != keepA) {
                                    mm.remove(removeA, removeB);
                                    change = true;
                                }
                            }
                        }
                        
                        if(change) {
                            break;
                        }
                    }
                    abort = true;                    
                }

                Multimap<A, B> argh = HashMultimap.create();
                for(Entry<A, B> xxx : mm.entries()) {
                    argh.put(xxx.getKey(), xxx.getValue());
                }
                
                
                Cluster<A, B, S> cluster = new Cluster<>(clusterKey, argh);
                                
                
                
                ClusterStack<A, B, S> newStack = new ClusterStack<>(stack, cluster);
                nextB(i + 1, baseSolution, newStack, completeMatch);
                
//                Stream<S> partialSolutions = solutionCombiner.apply(baseSolution, a, b);
//                partialSolutions.forEach(partialSolution -> {
//                    Combination<A, B, S> c = new Combination<>(a, b, partialSolution);
//                    CombinationStack<A, B, S> newStack = new CombinationStack<>(stack, c);
//
//                    // recurse
//                    nextB(i + 1, partialSolution, newStack, completeMatch);
//                });

                // restore
                for(Entry<A, B> e : clusterCandidateMapping.entries()) {
                    remaining.put(e.getKey(), e.getValue());
                }                
            }
        } else {
            completeMatch.accept(stack);
        }

    }   
}
