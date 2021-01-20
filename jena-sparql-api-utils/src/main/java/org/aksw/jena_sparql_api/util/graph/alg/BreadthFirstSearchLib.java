package org.aksw.jena_sparql_api.util.graph.alg;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import com.github.jsonldjava.shaded.com.google.common.collect.Streams;

public class BreadthFirstSearchLib {

	/**
	 * For a given collection of nodes return the collection of successors w.r.t.
	 * sucessorFn.
	 * The successor function can be used to filter items..
	 * 
	 * @param <T>
	 * @param <C>
	 * @param current
	 * @param successorFn
	 * @param collectorSupplier
	 * @return
	 */
	public static <T, C extends Collection<T>> Stream<C> stream(
			C current,
			Function<? super T, ? extends Stream<? extends T>> successorFn,
			Supplier<? extends Collector<T, ?, C>> collectorSupplier) {

		Iterator<C> bfsIt = new BreadthFirstSearchIterator<T, C>(current, successorFn, collectorSupplier);
		return Streams.stream(bfsIt);
	}

}
