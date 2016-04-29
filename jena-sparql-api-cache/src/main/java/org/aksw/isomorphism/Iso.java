package org.aksw.isomorphism;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;

// TODO Make the triplet class available from the nfa package
class Triplet<S, T> {
    
}



interface SolutionGeneratorCollection<X, Y> {
    SolutionGeneratorCollection<X, Y> partition(Map<X, Y> partialSolution);
    Foo<X, Y> pick();
}

/***
 * 
 * 
 * Note this class inherits from Entry as essentially its a Tuple2 and can
 * thus be used as such should that ever be needed
 * 
 * @author raven
 *
 * @param <X>
 * @param <Y>
 */
class Foo<X, Y>
    implements Entry<SolutionGenerator<X, Y>, SolutionGeneratorCollectionImpl<X, Y>>
{
    protected SolutionGenerator<X, Y> picked;
    protected SolutionGeneratorCollectionImpl<X, Y> remaining;
    
    public Foo(SolutionGenerator<X, Y> picked,
            SolutionGeneratorCollectionImpl<X, Y> remaining) {
        super();
        this.picked = picked;
        this.remaining = remaining;
    }
    public SolutionGenerator<X, Y> getPicked() {
        return picked;
    }
    public SolutionGeneratorCollectionImpl<X, Y> getRemaining() {
        return remaining;
    }
    
    @Override
    public SolutionGenerator<X, Y> getKey() {
        return picked;
    }
    @Override
    public SolutionGeneratorCollectionImpl<X, Y> getValue() {
        return remaining;
    }
    @Override
    public SolutionGeneratorCollectionImpl<X, Y> setValue(
            SolutionGeneratorCollectionImpl<X, Y> value) {
        throw new UnsupportedOperationException();
    }
    
    
}

class SolutionGeneratorCollectionImpl<X, Y>
    implements SolutionGeneratorCollection<X, Y>
{
    protected TreeMultimap<? extends Comparable<?>, SolutionGenerator<X, Y>> sizeToSolGen;
    
    public SolutionGeneratorCollectionImpl(
            TreeMultimap<? extends Comparable<?>, SolutionGenerator<X, Y>> sizeToSolGen) {
        super();
        this.sizeToSolGen = sizeToSolGen;
    }

    /**
     * Pick the estimated cheapest solution generator,
     * return a a solution generator collection with that generator removed
     * 
     * @return
     */
    public Foo<X, Y> pick() {
        Entry<? extends Comparable<?>, Collection<SolutionGenerator<X, Y>>> currEntry = sizeToSolGen.asMap().firstEntry();
        Comparable<?> currKey = currEntry.getKey();
        SolutionGenerator<X, Y> currSolGen = Iterables.getFirst(currEntry.getValue(), null);      
    
        TreeMultimap<Comparable<?>, SolutionGenerator<X, Y>> remaining = TreeMultimap.create(sizeToSolGen);
        remaining.remove(currKey, currSolGen);
        
        SolutionGeneratorCollectionImpl<X, Y> r = new SolutionGeneratorCollectionImpl<>(remaining);
        
        Foo<X, Y> result = new Foo<>(currSolGen, r);
        return result;
    }
    
    /**
     * Partition the content of the collection in regard to a partial solution.
     * This operation may return 'this'
     * 
     * @param partialSolution
     */
    public SolutionGeneratorCollectionImpl<X, Y> partition(Map<X, Y> partialSolution) {
        Collection<SolutionGenerator<X, Y>> tmp = sizeToSolGen.values().stream()
            .map(solGen -> solGen.partition(partialSolution))
            .flatMap(x -> x.stream())
            .collect(Collectors.toList());
        
        TreeMultimap<Long, SolutionGenerator<X, Y>> sizeToSolGen = Iso.indexSolutionGenerators(tmp);
        SolutionGeneratorCollectionImpl<X, Y> result = new SolutionGeneratorCollectionImpl<X, Y>(sizeToSolGen);
        
        return result;
    }
}


/**
 * State for generating a solution
 * 
 * @author raven
 *
 * @param <X>
 * @param <Y>
 */
class State<X, Y> {
   protected SolutionGeneratorCollection<X, Y> solGenCol;
   protected Map<X, Y> partialSolution;
   
   public Stream<Map<X, Y>> streamSolutions() {  

       // Make sure there are solution generators
       if(!sizeToSolGen.isEmpty()) {
           // Pick the smallest estimated solution generator and prepare the stream for sub-states
           
           
           // For every solution, create a sub state
           currSolGen
               .generateSolutions()
               .flatMap(partialSolution -> {
                   // TODO Somehow get a hold of dependent solutionGenerators such that they can be processed with priority                   
                   Collection<SolutionGenerator<X, Y>> successors = null;

                   Collection<SolutionGenerator<X, Y>> tmp = successors.stream()
                       .map(succ -> succ.partition(partialSolution))
                       .flatMap(x -> x.stream())
                       .collect(Collectors.toList());
                   
                   // Now descend into the next solution generators
                   
                   
                   return null;                    
               });
       }       
   }
}

public class Iso {

    /**
     * Repartition subsequent partitions while the predicate is true
     *
     * This unifies three common use cases:
     * - k = 0   : Do not repartition at all
     * - k = 1   : Repartition the next largest equivalence class
     * - k = null: Repartition all equivalence classes
     *
     */
//    public static <N, M> Entry<? extends Collection<M>, ? extends Collection<M>>
//        nextEquivClassRepartitionK(TreeMultimap<K, V> equivClasses, BiPredicate<Integer, Entry<? extends Collection<M>, ? extends Collection<M>>>) {
//        return null;
//    }
//

    
    public static <X, Y> TreeMultimap<Long, SolutionGenerator<X, Y>> indexSolutionGenerators(Collection<SolutionGenerator<X, Y>> solGens) {
        TreeMultimap<Long, SolutionGenerator<X, Y>> result = TreeMultimap.create();

        for(SolutionGenerator<X, Y> solutionGenerator : solGens) {
            long size = solutionGenerator.estimateSize();
            result.put(size, solutionGenerator);
        }

        return result;
    }

    public static <X, Y> Stream<Map<X, Y>> vf2(Collection<SolutionGenerator<X, Y>> solutionGenerators, Function<SolutionGenerator<X, Y>, Collection<SolutionGenerator<X, Y>>> selector) {
        // Order solution generators by their estimated sizes

        TreeMultimap<Long, SolutionGenerator<X, Y>> sizeToSolutionGenerator = TreeMultimap.create();

        for(SolutionGenerator<X, Y> solutionGenerator : solutionGenerators) {
            long size = solutionGenerator.estimateSize();
            sizeToSolutionGenerator.put(size, solutionGenerator);
        }
        
        //Multimaps.
        // If there are a zero sized equiv classes, there can be no solution mappings
        // If there are no solution generators return the empty solution (which is different from null - which indicates the absence of a solution)
        if(sizeToSolutionGenerator.isEmpty()) {
        
            // Take the smallest estimated solution generator and start generating solutions
            SolutionGenerator<X, Y> solutionGenerator = sizeToSolutionGenerator.asMap().firstEntry().getValue().iterator().next();
            
            solutionGenerator
                .generateSolutions()
                .flatMap(partialSolution -> {
                    // TODO Somehow get a hold of dependent solutionGenerators such that they can be processed with priority

                    Collection<SolutionGenerator<X, Y>> successors = null;
                    
                    Collection<SolutionGenerator<X, Y>> tmp = successors.stream()
                        .map(succ -> succ.partition(partialSolution))
                        .flatMap(x -> x.stream())
                        .collect(Collectors.toList());
                    
                    // Now descend into the next solution generators
                    
                    
                    return null;                    
                });
        }
        
        
        return null;
    }

    
    public static partition()

    /**
     * This is a generalized version of VF2:
     * 
     * Given two sets of items A and B, in addition to matching items (such as nodes in a graph) directly,
     * it is possible to create mappings between *attributes* X and Y of these items.
     * For instance, if you are matching expressions, than this implementation can yield a mapping of variables appearing in it. 
     * 
     * 
     * Relations between objects: In a graph setting, nodes are connected via edges.
     * We take advantage of this fact, that whenever two items are matched, we can process the equivalence classes
     * that contain the related objects and see if these classes become smaller than any other equiv classes so far.
     * 
     * It is fine to leave
     * 
     * The algorithm can be viewed as to unify solution *generating* and solution *validating* elements:
     * Consider a graph: If its edges are unlabeled, then equivalence classes of vertices can be built based on in/out degree
     * If however, the degrees are all the some, yet the edge labels vary significantly, it is possible to create equivalence classes from them 
     * 
     * 
     * But this means, we have to abstract from the concrete objects we are matching (nodes, edges, quads, expressions) - and hence
     * the algorithm may only operate on equivalence class objects and partial solutions.
     * 
     *
     * @param srcs The items to match with tgts
     * @param tgts
     * @param matchToPartialSolution
     * @param srcToEquivClass
     * @param tgtToEquivClass
     * @param partialSolution
     * @param candidatePairingGenerator A function that generates candidate
     *          pairings on members of of an equivalence class in regard to a partial solution
     * @return
     */
//    public static <A, B, PA, PB, X, Y, E extends Comparable<E>> Stream<Map<M, N>> iso(
//            Collection<A> srcs,
//            Collection<B> tgts,
//            BiFunction<A, B, Entry<X, Y>> matchToPartialSolution, // Function that returns a partial solution by matching A with B
//            Function<A, Collection<Triplet<A, PA>>> aRelations, // related items with A - could be predecessor and successor nodes
//            Function<B, Collection<Triplet<B, PB>>> bRelations, // related items with B
//            Function<? super A, E> srcToEquivClass,
//            Function<? super B, E> tgtToEquivClass,
//            Map<M, N> partialSolution,
//
//            TriFunction<
//                Collection<? extends M>, // In:  A collection of items from M
//                Collection<? extends N>, // In:  A collection of items from N
//                Map<M, N>,                       // In:  A partial solution
//                TreeMultimap<Number, Entry<Collection<M>, Collection<N>>>              // Out: A stream of candidate pairings
//            > equivClassPartitioner,
//
//            TriFunction<
//                ? super Collection<? extends M>, // In:  A collection of items from M
//                ? super Collection<? extends N>, // In:  A collection of items from N
//                Map<M, N>,                       // In:  A partial solution
//                Stream<Entry<M, N>>              // Out: A stream of candidate pairings
//            > candidatePairingGenerator)
//    {
//        Multimap<E, M> srcEquivClasses = indexItemsByEquivClass(srcs, srcToEquivClass);
//        Multimap<E, N> tgtEquivClasses = indexItemsByEquivClass(tgts, tgtToEquivClass);
//
//        Stream<Map<M, N>> result = iso(srcEquivClasses, tgtEquivClasses);
//        return result;
//    }



    public static <M, N, E extends Comparable<E>> Stream<Map<M, N>> iso(
            Multimap<E, M> srcEquivClasses,
            Multimap<E, N> tgtEquivClasses)
    {
        Stream<Map<M, N>> result;

        // Create a list of all equiv classes and order it by the number of possible pairings
        // We use a tree multi map for that purpose
        TreeMultimap<Integer, E> sizeToEquivClasses = TreeMultimap.create();

        Set<E> equivClasses = Sets.union(srcEquivClasses.keySet(), tgtEquivClasses.keySet());
        for(E equivClass : equivClasses) {
            Collection<M> srcItems = srcEquivClasses.get(equivClass);
            Collection<N> tgtItems = tgtEquivClasses.get(equivClass);


        }


        // If there are a zero sized equiv classes, there can be no solution mappings
        Collection<E> zeroSizedEquivClasses = sizeToEquivClasses.get(0);
        if(!zeroSizedEquivClasses.isEmpty()) {
            result = Collections.<Map<M, N>>emptySet().stream();
        } else {


        }



        return null;

    }


    public static <S, E> Multimap<E, S> indexItemsByEquivClass(Collection<S> items, Function<? super S, E> itemToEquivClass) {
        Multimap<E, S> result = HashMultimap.create();

        for(S item : items) {
            E equivClass = itemToEquivClass.apply(item);
            result.put(equivClass, item);
        }

        return result;
    }
}
