package org.aksw.isomorphism;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BinaryOperator;
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



interface SolutionGeneratorCollection<S> {
    SolutionGeneratorCollection<S> partition(S partialSolution);
    Foo<S> pick();
    boolean isEmpty();
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
class Foo<S>
    implements Entry<SolutionGenerator<S>, SolutionGeneratorCollectionImpl<S>>
{
    protected SolutionGenerator<S> picked;
    protected SolutionGeneratorCollectionImpl<S> remaining;
    
    public Foo(SolutionGenerator<S> picked,
            SolutionGeneratorCollectionImpl<S> remaining) {
        super();
        this.picked = picked;
        this.remaining = remaining;
    }
    public SolutionGenerator<S> getPicked() {
        return picked;
    }
    public SolutionGeneratorCollectionImpl<S> getRemaining() {
        return remaining;
    }
    
    @Override
    public SolutionGenerator<S> getKey() {
        return picked;
    }
    @Override
    public SolutionGeneratorCollectionImpl<S> getValue() {
        return remaining;
    }
    @Override
    public SolutionGeneratorCollectionImpl<S> setValue(
            SolutionGeneratorCollectionImpl<S> value) {
        throw new UnsupportedOperationException();
    }
    
    
}

class SolutionGeneratorCollectionImpl<S>
    implements SolutionGeneratorCollection<S>
{
    protected TreeMultimap<? extends Comparable<?>, SolutionGenerator<S>> sizeToSolGen;
    
    public SolutionGeneratorCollectionImpl(
            TreeMultimap<? extends Comparable<?>, SolutionGenerator<S>> sizeToSolGen) {
        super();
        this.sizeToSolGen = sizeToSolGen;
    }

    /**
     * Pick the estimated cheapest solution generator,
     * return a a solution generator collection with that generator removed
     * 
     * @return
     */
    public Foo<S> pick() {
        Entry<? extends Comparable<?>, Collection<SolutionGenerator<S>>> currEntry = sizeToSolGen.asMap().firstEntry();
        Comparable<?> currKey = currEntry.getKey();
        SolutionGenerator<S> currSolGen = Iterables.getFirst(currEntry.getValue(), null);      
    
        TreeMultimap<Comparable<?>, SolutionGenerator<S>> remaining = TreeMultimap.create(sizeToSolGen);
        remaining.remove(currKey, currSolGen);
        
        SolutionGeneratorCollectionImpl<S> r = new SolutionGeneratorCollectionImpl<>(remaining);
        
        Foo<S> result = new Foo<>(currSolGen, r);
        return result;
    }
    
    /**
     * Partition the content of the collection in regard to a partial solution.
     * This operation may return 'this'
     * 
     * @param partialSolution
     */
    public SolutionGeneratorCollectionImpl<S> partition(S partialSolution) {
        Collection<SolutionGenerator<S>> tmp = sizeToSolGen.values().stream()
            .map(solGen -> solGen.partition(partialSolution))
            .flatMap(x -> x.stream())
            .collect(Collectors.toList());
        
        TreeMultimap<Long, SolutionGenerator<S>> sizeToSolGen = Iso.indexSolutionGenerators(tmp);
        SolutionGeneratorCollectionImpl<S> result = new SolutionGeneratorCollectionImpl<S>(sizeToSolGen);
        
        return result;
    }

    @Override
    public boolean isEmpty() {
        boolean result = sizeToSolGen.isEmpty();
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
class State<S> {
   protected SolutionGeneratorCollection<S> solGenCol;
   protected S baseSolution;
   protected BinaryOperator<S> solutionCombiner;
   
   public State(SolutionGeneratorCollection<S> solGenCol, S baseSolution,
        BinaryOperator<S> solutionCombiner) {
    super();
    this.solGenCol = solGenCol;
    this.baseSolution = baseSolution;
    this.solutionCombiner = solutionCombiner;
   }


    public Stream<S> streamSolutions() {

        Foo<S> pick = solGenCol.pick();

        SolutionGenerator<S> picked = pick.getPicked();
        SolutionGeneratorCollectionImpl<S> remaining = pick.getRemaining();

        Stream<S> result = picked
            .generateSolutions()
            .flatMap(solutionContribution -> {
                S partialSolution = solutionCombiner.apply(baseSolution, solutionContribution);

                Stream<S> r;
                // If the partial solution is null, then indicate the
                // absence of a solution by returning a stream that yields
                // null as a 'solution'
                if (partialSolution == null) {
                    r = Collections.<S> singleton(null).stream();
                } else {
                    // This step is optional: it refines solution generators
                    // based on the current partial solution
                    // Depending on your setting, this can give a
                    // performance boost or penalty
                    SolutionGeneratorCollectionImpl<S> repartition = remaining.partition(partialSolution);

                    if (repartition.isEmpty()) {
                        r = Collections.<S> emptySet().stream();
                    } else {
                        State<S> nextState = new State<S>(repartition, baseSolution, solutionCombiner);
                        r = nextState.streamSolutions();
                    }
                }
                return r;
            });

        return result;
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

    
    public static <S> TreeMultimap<Long, SolutionGenerator<S>> indexSolutionGenerators(Collection<SolutionGenerator<S>> solGens) {
        TreeMultimap<Long, SolutionGenerator<S>> result = TreeMultimap.create();

        for(SolutionGenerator<S> solutionGenerator : solGens) {
            long size = solutionGenerator.estimateSize();
            result.put(size, solutionGenerator);
        }

        return result;
    }



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
