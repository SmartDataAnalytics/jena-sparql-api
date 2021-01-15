package org.aksw.jena_sparql_api.util.graph.alg;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.apache.jena.ext.com.google.common.collect.AbstractIterator;


public class BreadthFirstSearchIterator<T, C extends Collection<T>>
	extends AbstractIterator<C> {

	protected C current;
	protected Function<? super T, ? extends Stream<? extends T>> successorFn;
	protected Supplier<? extends Collector<T, ?, C>> collectorSupplier;
	protected Set<? super T> seen;

	protected C nextResult = null;
	
	public BreadthFirstSearchIterator(
			C current,
			Function<? super T, ? extends Stream<? extends T>> successorFn,
			Supplier<? extends Collector<T, ?, C>> collectorSupplier) {
		super();
		this.current = current;
		this.successorFn = successorFn;
		this.collectorSupplier = collectorSupplier;
		this.seen = new HashSet<T>();
	}

	@Override
	protected C computeNext() {
		C result;
		if (current.isEmpty()) {
			result = endOfData();
		} else {
			result = current;

			Collector<T, ?, C> collector = collectorSupplier.get(); 
			current = current.stream()
					.flatMap(item -> successorFn.apply(item).filter(x -> !seen.contains(x)))
					.collect(collector);
		}
		
		return result;
	}
}