package org.aksw.isomorphism;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;



public class Iso {

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
