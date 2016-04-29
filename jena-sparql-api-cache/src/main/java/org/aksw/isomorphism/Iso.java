package org.aksw.isomorphism;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Stream;

import com.codepoetics.protonpack.functions.TriFunction;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;

class DefaultState<M, N> {
    protected Map<N, M> partialSolution;
    protected TreeMultimap<Comparable<?>, Entry<Collection<M>, Collection<N>>> remainingEquivClasses;


    public static <M, N> Map<M, N> getPartialSolution(DefaultState<M, N> state) {
        return state.getPartialSolution();
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
    public static <N, M> Entry<? extends Collection<M>, ? extends Collection<M>>
        nextEquivClassRepartitionK(TreeMultimap<K, V> equivClasses, BiPredicate<Integer, Entry<? extends Collection<M>, ? extends Collection<M>>>) {
        return null;
    }






    /**
     * Given two collections 'srcs' and 'tgts' with items of types M and N, find a mapping
     * from the items of the former to the latter.
     *
     * @param srcs
     * @param tgts
     * @param srcToEquivClass
     * @param tgtToEquivClass
     * @param partialSolution
     * @param candidatePairingGenerator A function that generates candidate
     *          pairings on members of of an equivalence class in regard to a partial solution
     * @return
     */
    public static <M, N, E extends Comparable<E>> Stream<Map<M, N>> iso(
            Collection<M> srcs,
            Collection<N> tgts,
            Function<? super M, E> srcToEquivClass,
            Function<? super N, E> tgtToEquivClass,
            Map<M, N> partialSolution,

            TriFunction<
                Collection<? extends M>, // In:  A collection of items from M
                Collection<? extends N>, // In:  A collection of items from N
                Map<M, N>,                       // In:  A partial solution
                TreeMultimap<Number, Entry<Collection<M>, Collection<N>>>              // Out: A stream of candidate pairings
            > equivClassPartitioner,

            TriFunction<
                ? super Collection<? extends M>, // In:  A collection of items from M
                ? super Collection<? extends N>, // In:  A collection of items from N
                Map<M, N>,                       // In:  A partial solution
                Stream<Entry<M, N>>              // Out: A stream of candidate pairings
            > candidatePairingGenerator)
    {
        Multimap<E, M> srcEquivClasses = indexItemsByEquivClass(srcs, srcToEquivClass);
        Multimap<E, N> tgtEquivClasses = indexItemsByEquivClass(tgts, tgtToEquivClass);

        Stream<Map<M, N>> result = iso(srcEquivClasses, tgtEquivClasses);
        return result;
    }



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
