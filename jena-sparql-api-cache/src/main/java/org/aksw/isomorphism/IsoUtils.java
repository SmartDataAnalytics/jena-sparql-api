package org.aksw.isomorphism;

import java.util.ArrayList;
import java.util.Collection;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.google.common.collect.TreeMultimap;

public class IsoUtils {

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


        public static <S> TreeMultimap<Long, Problem<S>> indexSolutionGeneratorsOld(Collection<Problem<S>> solGens) {
            TreeMultimap<Long, Problem<S>> result = TreeMultimap.create();

            for(Problem<S> solutionGenerator : solGens) {
                long size = solutionGenerator.getEstimatedCost();
                result.put(size, solutionGenerator);
            }

            return result;
        }

        public static <S> NavigableMap<Long, Collection<Problem<S>>> indexSolutionGenerators(Collection<Problem<S>> solGens) {
            NavigableMap<Long, Collection<Problem<S>>> result = new TreeMap<>();

            for(Problem<S> solutionGenerator : solGens) {
                long size = solutionGenerator.getEstimatedCost();

                result.computeIfAbsent(size, (x) -> new ArrayList<>()).add(solutionGenerator);

                //result.put(size, solutionGenerator);
            }

            return result;
        }

}
