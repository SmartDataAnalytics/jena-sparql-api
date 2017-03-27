package org.aksw.jena_sparql_api.cache.tests;

import java.util.function.Function;
import java.util.stream.Stream;

public class RewriteUtils {
	// TODO The following approaches could also be seen as reductions:
	// listCandidates returns a stream of reduction rules that are applicable to the baseItem
	// applyCandidate then applies the reduction rule
	// It should not happen that the exact same reduction occurs more than once (otherwise we may not terminate)
	//   We could relax this rule that the baseItems must occur more than once

	public static <T, C> Stream<T> exhaustiveRewrite(
			T baseItem,
			Function<T, Stream<T>> reductions
			//Function<T, Stream<C>> listCandidates,
			//BiFunction<T, C, T> applyCandidate,
			//Collection<C> usedCandidates
	) {
		// Terminal items are those that do not have (further) candidates
		Stream<T> result = Stream.of(baseItem).flatMap(item -> {
			//Stream<C> candidates = listCandidates.apply(item);

			boolean[] empty = new boolean[]{true};

			// We perform conditional concatenation: If the stream turned out to be empty, we append our element
			Stream<T> nestedItems = reductions.apply(item)//candidates
					//.filter(c -> ! usedCandidates.contains(c))
					.peek(i -> empty[0] = false)
					.flatMap(c -> {
						//usedCandidates.add(c);
						//T nextItem = applyCandidate.apply(item, c);

						Stream<T> r = exhaustiveRewrite(c, reductions);
						//Stream<T> r = exhaustiveRewrite(nextItem, listCandidates, applyCandidate, usedCandidates);

						return r;
					});

			Stream<T> s = Stream.concat(nestedItems,
					Stream.of(item).filter(x -> empty[0] == true));

			return s;
		});

		return result;
	}

	public static <T, C, X extends Comparable<X>> T exhaustiveRewrite(
			T item,
			Function<T, Stream<T>> reductions,
			//Function<T, Stream<C>> listCandidates,
			//BiFunction<T, C, T> applyCandidate,
			Function<T, X> cost
			) {

		//, usedCandidates
		//Collection<C> usedCandidates = new HashSet<>();
		Stream<T> stream = exhaustiveRewrite(item, reductions);
		T result = stream
				.min((a, b) -> {
					X ca = cost.apply(a);
					X cb = cost.apply(b);
					int r = ca.compareTo(cb);
					return r;
				})
				.orElse(null);
		return result;
	}

	// enhance :- x = listCandidates.apply(item).findFirst().orElse(null); x != null ? applyCandidate(item, x) : null
	public static <T, C> Stream<T> greedyRewrite(
			T item,
			Function<T, Stream<T>> reductions
			//Function<T, Stream<C>> listCandidates,
			//BiFunction<T, C, T> applyCandidate
	) {
		//Stream<T> result = it
		return Stream.of(item).map(i -> greedyRewriteCore(item, reductions));

		//Collection<C> usedCandidates = new HashSet<>();

	}


	public static <T, C> T greedyRewriteCore(
			T item,
			Function<T, Stream<T>> reductions
	) {
		T result = item;
		for(;;) {
            // TODO: enhance must be refactored into:
			// - getCandidates -
			// - applyCandidate
//			 C candidate = listCandidates.apply(item)
//					 .filter(c -> ! usedCandidates.contains(c))
//					 .findFirst()
//					 .orElse(null);
			//T tmp = enhance.apply(item).findFirst().orElse(null);
			T tmp = reductions.apply(result).findFirst().orElse(null);
			if(tmp == null) {
				break;
			}

			result = tmp;
//
//			usedCandidates.add(candidate);
//			result = applyCandidate.apply(item, candidate);

		}
		return result;
	}
}