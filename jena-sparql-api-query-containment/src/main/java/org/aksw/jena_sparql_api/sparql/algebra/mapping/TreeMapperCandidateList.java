package org.aksw.jena_sparql_api.sparql.algebra.mapping;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.aksw.combinatorics.collections.Cluster;
import org.aksw.combinatorics.collections.ClusterStack;
import org.aksw.commons.collections.multimaps.BiHashMultimap;
import org.aksw.commons.collections.trees.Tree;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
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
public class TreeMapperCandidateList<A, B>
    //extends KPermutationsOfNCallbackBase<A, B, S>
{
    protected List<A> as;

    /**
     * A multimap that maps each item of A to its candidates in B.
     * Whenever an 'a' is mapped to a 'b', the 'b' is no longer available for
     * further mappings of 'a'.
     *
     */
    //protected BiHashMultimap<A, B> remaining;

    protected Tree<A> aTree;
    protected Tree<B> bTree;
    protected Multimap<A, B> childMapping;

    protected Multimap<A, B> remainingParentMapping;

    public TreeMapperCandidateList(
            Tree<A> aTree,
            Tree<B> bTree,
            List<A> as,
            Multimap<A, B> childMapping,
            Multimap<A, B> parentMapping
        ) {
        this.as = as;
        this.childMapping = childMapping;
        this.aTree = aTree;
        this.bTree = bTree;
        this.remainingParentMapping = parentMapping;
    }

    public Stream<ClusterStack<A, B, Entry<A, B>>> stream() {//S baseSolution) {
        List<ClusterStack<A, B, Entry<A, B>>> list = new ArrayList<>();

        run((stack) -> list.add(stack));

        Stream<ClusterStack<A, B, Entry<A, B>>> result = list.stream();
        return result;
    }




    public void run(Consumer<ClusterStack<A, B, Entry<A, B>>> completeMatch) {
        boolean isEmpty = as.isEmpty(); //remainingA.successor.isTail();
        if(!isEmpty) {
            recurse(0, null, completeMatch);
        }
    }

    //@Override
    public void recurse(int i, ClusterStack<A, B, Entry<A, B>> stack, Consumer<ClusterStack<A, B, Entry<A, B>>> completeMatch) {


        // Instead of iterating all as, we iterate over the parents of the bs that they map to
        // Find out to which parents of B the items of a can map.
        // Then, pick
        if(i < as.size()) {
            A a = as.get(i);

            // TODO Maybe with more clever data structures we can get rid of having to create a copy
            List<B> bs = new ArrayList<>(remainingParentMapping.get(a));

            // Pick a 'b'
            for(B b : bs) {

                Entry<A, B> clusterKey = new SimpleEntry<>(a, b);
//
//                // Get the clusterKey for b
//                // (for tree-children that would usually be their parent)
//                S clusterKey = bToClusterKey.apply(b);
//
//                // Get all b's that are valid within the cluster
//                // (Note: This assumes that this set is known beforehand)
//                // (for a tree, that would be the children of the parent)
//                Collection<B> candidateBsInCluster = clusterKeyToBs.apply(clusterKey);
//
//                // Get all a's in relation to the candidate b's
//                //List<A> affectedAs = new ArrayList<>();
//                //Map<B, List<A>> bToAffectedAs;
//                //List<A> affectedAs = new ArrayList<>();
//                Multimap<A, B> clusterCandidateMapping = HashMultimap.create();
//                for(B candidateB : candidateBsInCluster) {
//                    Set<A> affectedAs = remaining.getInverse().get(candidateB);
//
//                    for(A affectedA : affectedAs) {
//                        clusterCandidateMapping.put(affectedA, candidateB);
//                    }
//                }

                // The candidate mapping is the mapping of all children of the matched parents
                // in accordance with child candidate mapping
                // If the parent is null, it means that the tree only consists of the root node

                List<A> aChildren = a == null ? Collections.singletonList(aTree.getRoot()) : new ArrayList<>(aTree.getChildren(a));
                List<B> bChildren = b == null ? Collections.singletonList(bTree.getRoot()) : new ArrayList<>(bTree.getChildren(b));

                boolean unsatisfiable = false;

                Multimap<A, B> clusterCandidateMapping = HashMultimap.create();
                for(A aChild : aChildren) {
                    Collection<B> bCands = childMapping.get(aChild);
                    // intersect the bCands with the b children
                    Set<B> bRemaining = new HashSet<>(bCands);
                    bRemaining.retainAll(bChildren);

                    if(bRemaining.isEmpty()) {
                        unsatisfiable = true;
                        break;
                    }

                    clusterCandidateMapping.putAll(aChild, bRemaining);
                    // All children of A must have candidates
                }

                if(!unsatisfiable) {
                    //BiHashMultimap<A, B> mm = KPermutationsOfNUtils.create(clusterCandidateMapping);
                    // Collect all single-mappings
                    BiMap<B, A> bToOnlyA = HashBiMap.create();

                    for(Entry<A, Collection<B>> e : clusterCandidateMapping.asMap().entrySet()) {
                        A ax = e.getKey();
                        if(e.getValue().size() == 1) {
                            B bx = e.getValue().iterator().next();

                            if(bToOnlyA.containsKey(bx)) {
                                unsatisfiable = true;
                                break;
                            } else {
                                bToOnlyA.put(bx, ax);
                            }
                        }
                    }

                    //if(!unsatisifable) {
                    while(!bToOnlyA.isEmpty()) {
                        BiMap<B, A> nextBToOnlyA = HashBiMap.create();

                        Collection<B> bRemovals = bToOnlyA.keySet();
                        //for(Entry<A, Collection<B>> e : clusterCandidateMapping.asMap().entrySet()) {
                        Map<A, Collection<B>> m = clusterCandidateMapping.asMap();

                        // Note: We have to copy the keyset because changing collection of the entries' value
                        // causes a cme.
                        for(A ax : new HashSet<>(m.keySet())) {
                            Collection<B> bxs = m.get(ax);
                            int sizeBefore = bxs.size();

                            // remove all bs that only map to a single a
                            bxs.removeAll(bRemovals);

                            B restoreB = bToOnlyA.inverse().get(ax);
                            if(restoreB != null) {
                                bxs.add(restoreB);
                            }

                            int sizeAfter = bxs.size();

                            if(sizeAfter == 1 && sizeBefore != sizeAfter) {
                                B newB = bxs.iterator().next();
                                nextBToOnlyA.put(newB, ax);
                            }
                        }

                        bToOnlyA = nextBToOnlyA;
                    }
                }

                // Recurse if still satisfiable
                if(!unsatisfiable) {

                    // Update state: Remove the clusterCandidateMapping from the remaining set
                    remainingParentMapping.remove(a, b);
//                    for(Entry<A, B> e : clusterCandidateMapping.entries()) {
//                        remainingParentMapping.remove(e.getKey(), e.getValue());
//                    }

                    Cluster<A, B, Entry<A, B>> cluster = new Cluster<>(clusterKey, clusterCandidateMapping);
                    ClusterStack<A, B, Entry<A, B>> newStack = new ClusterStack<>(stack, cluster);

                    recurse(i + 1, newStack, completeMatch);

                    // Restore state
                    remainingParentMapping.put(a, b);
//                    for(Entry<A, B> e : clusterCandidateMapping.entries()) {
//                        remainingParentMapping.put(e.getKey(), e.getValue());
//                    }
                }
            }
        } else {
            completeMatch.accept(stack);
        }

    }
}
