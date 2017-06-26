package org.aksw.isomorphism;

/**
 * Utility class for finding (sub-)isomorphic mappings
 *
 * TODO Rename the variables; they stem from a specific implementation that was generalized here.
 *
 *
 * @author raven
 *
 */
//public class IsoMapUtils {
//
//    public static <A, B, X, Y> Stream<Map<X, Y>> createSolutionStream(
//            Collection<A> candQuads,
//            Collection<B> queryQuads,
//            TriFunction<A, B, Map<X, Y>, Stream<Map<X, Y>>> createPartialSolution,
//            Map<X, Y> baseSolution) {
//        // Reminder: We need to find a mapping from candidate vars to those of the query
//        // The cache must have fewer quads than the query to be applicable
//        // so we take all combinations and permutations of the *query*'s quads and match them with the cache quads
////        Collection<Quad> candQuads = quadGroup.getKey();
////        Collection<Quad> queryQuads = quadGroup.getValue();
//
////        ICombinatoricsVector<B> queryQuadVector = Factory.createVector(queryQuads);
////        Generator<B> queryQuadCombis = Factory.createSimpleCombinationGenerator(queryQuadVector, candQuads.size());
//
//        StateCombinatoricCallback.createKPermutationsOfN(candQuads, queryQuads, baseSolution, createPartialSolution);
//        
//        Stream<Map<X, Y>> result = StreamSupport
//            .stream(queryQuadCombis.spliterator(), false)
//            .flatMap(tmp -> {
//                // Not sure if we need a copy here
//                ICombinatoricsVector<B> queryQuadCombi = Factory.createVector(tmp);
//                Generator<B> permutations = Factory.createPermutationGenerator(queryQuadCombi);
//                Stream<ICombinatoricsVector<B>> perm = StreamSupport.stream(permutations.spliterator(), false);
//
//                Stream<Map<X, Y>> r = perm
//                        .map(tmpQueryQuads -> reduceToMap(candQuads, tmpQueryQuads, createPartialSolution, baseSolution));
//
//                //perm.peek(test -> System.out.println("PEEKABOO: " + test));
//
//                return r;
//            })
//            .filter(Objects::nonNull);
//
//
//        Collection<Map<X, Y>> r = result.collect(Collectors.toList());
//        if(r.isEmpty()) {
//            r = Collections.singleton(Collections.emptyMap());
//        }
//        //System.out.println("solutions: " + r);
//        result = r.stream();
//
//
//        return result;
//    }
//
//
//    public static <A, B, X, Y> Map<X, Y> reduceToMap(
//            Iterable<A> candQuads,
//            Iterable<B> queryQuads,
//            TriFunction<A, B, Map<X, Y>, Stream<Map<X, Y>>> createPartialSolution,
//            Map<X, Y> baseSolution
//            ) {
//        Map<X, Y> result = StreamUtils
//            .zip(
//                    StreamSupport.stream(candQuads.spliterator(), false),
//                    StreamSupport.stream(queryQuads.spliterator(), false),
//                    (a, b) -> new SimpleEntry<A, B>(a, b))
//            .flatMap(e -> createPartialSolution.apply(e.getKey(), e.getValue(), baseSolution))
//            .reduce(new HashMap<X, Y>(), MapUtils::mergeInPlaceIfCompatible);
//
//        return result;
//    }
//}
