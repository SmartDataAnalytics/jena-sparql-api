package org.aksw.isomorphism;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.aksw.commons.collections.MapUtils;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import com.codepoetics.protonpack.StreamUtils;
import com.codepoetics.protonpack.functions.TriFunction;

/**
 * Utility class for finding (sub-)isomorphic mappings
 *
 * TODO Rename the variables; they stem from a specific implementation that was generalized here.
 *
 *
 * @author raven
 *
 */
public class IsoMapUtils {

    public static <A, B, X, Y> Stream<Map<X, Y>> createSolutionStream(
            Collection<A> candQuads,
            Collection<B> queryQuads,
            TriFunction<A, B, Map<X, Y>, Stream<Map<X, Y>>> createPartialSolution,
            Map<X, Y> baseSolution) {
        // Reminder: We need to find a mapping from candidate vars to those of the query
        // The cache must have fewer quads than the query to be applicable
        // so we take all combinations and permutations of the *query*'s quads and match them with the cache quads
//        Collection<Quad> candQuads = quadGroup.getKey();
//        Collection<Quad> queryQuads = quadGroup.getValue();

        ICombinatoricsVector<B> queryQuadVector = Factory.createVector(queryQuads);
        Generator<B> queryQuadCombis = Factory.createSimpleCombinationGenerator(queryQuadVector, candQuads.size());

        Stream<Map<X, Y>> result = StreamSupport
            .stream(queryQuadCombis.spliterator(), false)
            .flatMap(tmp -> {
                // Not sure if we need a copy here
                ICombinatoricsVector<B> queryQuadCombi = Factory.createVector(tmp);
                Generator<B> permutations = Factory.createPermutationGenerator(queryQuadCombi);
                Stream<ICombinatoricsVector<B>> perm = StreamSupport.stream(permutations.spliterator(), false);

                Stream<Map<X, Y>> r = perm
                        .map(tmpQueryQuads -> reduceToMap(candQuads, tmpQueryQuads, createPartialSolution, baseSolution));

                //perm.peek(test -> System.out.println("PEEKABOO: " + test));

                return r;
            })
            .filter(Objects::nonNull);


        Collection<Map<X, Y>> r = result.collect(Collectors.toList());
        //System.out.println("solutions: " + r);
        result = r.stream();


        return result;
    }


    public static <A, B, X, Y> Map<X, Y> reduceToMap(
            Iterable<A> candQuads,
            Iterable<B> queryQuads,
            TriFunction<A, B, Map<X, Y>, Stream<Map<X, Y>>> createPartialSolution,
            Map<X, Y> baseSolution
            ) {
        Map<X, Y> result = StreamUtils
            .zip(
                    StreamSupport.stream(candQuads.spliterator(), false),
                    StreamSupport.stream(queryQuads.spliterator(), false),
                    (a, b) -> new SimpleEntry<A, B>(a, b))
            .flatMap(e -> createPartialSolution.apply(e.getKey(), e.getValue(), baseSolution))
            .reduce(new HashMap<X, Y>(), IsoMapUtils::mergeInPlaceIfCompatible);

        return result;
    }


    /**
     *
     *
     * @param inout the map being changed in place - may be null
     * @param addition the mappings about to be added
     * @return the provided map or null if the merge was incompatible
     */
    public static <X, Y> Map<X, Y> mergeInPlaceIfCompatible(Map<X, Y> inout, Map<X, Y> addition) {
        Map<X, Y> result = null;
        if(inout != null && addition != null) {
            boolean isCompatible = MapUtils.isPartiallyCompatible(inout, addition);
            if(isCompatible) {
                inout.putAll(addition);
                result = inout;
            }
        }
        return result;
    }
}
