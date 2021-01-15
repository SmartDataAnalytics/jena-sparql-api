package org.aksw.jena_sparql_api.util.graph.alg;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import com.github.jsonldjava.shaded.com.google.common.collect.Streams;

public class BreadthFirstSearchLib {

	public static <T, C extends Collection<T>> Stream<C> stream(
			C current,
			Function<? super T, ? extends Stream<? extends T>> successorFn,
			Supplier<? extends Collector<T, ?, C>> collectorSupplier) {

		Iterator<C> bfsIt = new BreadthFirstSearchIterator<T, C>(current, successorFn, collectorSupplier);
		return Streams.stream(bfsIt);
	}

}
