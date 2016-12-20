package org.aksw.jena_sparql_api.rdf_stream.core;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.rdf_stream.enhanced.ModelFactoryEnh;
import org.aksw.jena_sparql_api.rdf_stream.enhanced.ResourceEnh;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.ResourceUtils;

public class RdfStreamOps {
    public static ResourceEnh copyResourceClosureIntoModelEnh(Resource task) {
		Model m = ModelFactoryEnh.createModel();
		m.add(ResourceUtils.reachableClosure(task));
		ResourceEnh result = task.inModel(m).as(ResourceEnh.class);
		return result;
    }

	public static <T extends Resource> Function<Supplier<Stream<T>>, Supplier<Stream<T>>>
		start()
	{
		return (ss) -> (() -> ss.get());
	}

//	public static <T extends Resource> Function<Supplier<Stream<T>>, Supplier<Stream<T>>>
//		start(Class<T> clazz)
//	{
//		return (ss) -> (() -> ss.get());
//	}


	/**
	 * starts with copyResourceClosureIntoModelEnh
	 *
	 * For each item in the stream, map its closure to a new resource
	 * @param stream
	 * @param p
	 * @return
	 */
	public static <T extends Resource> Function<Supplier<Stream<T>>, Supplier<Stream<ResourceEnh>>>
		startWithCopy()
	{
		//return (ss) -> (() -> ss.get().map(RdfStreamOps::copyResourceClosureIntoModelEnh));
		return RdfStreamOps.<T>start().andThen(map(RdfStreamOps::copyResourceClosureIntoModelEnh));
	}


	public static <T extends Resource> Supplier<Stream<T>>
		withIndex(Supplier<Stream<T>> stream, Property p)
	{
		int i = 0;

		return () -> stream.get().peek(r -> r.addLiteral(p, new Integer(i)));
	}


	/**
	 * repeat repeats the provided supplied n times and to each item
	 * adds a property indicating the repetation
	 *
	 * note that this is different from withIndex:
	 * within a repetation, the repetation count stays the same for each item, whereas the index is increased.
	 *
	 * @param n
	 * @param property
	 * @return
	 */
	public static <T extends Resource> Function<Supplier<Stream<T>>, Supplier<Stream<T>>>
		repeat(int n, Property property, int offset)
	{
		return (ss) -> (() -> LongStream.range(0, n).boxed()
				.flatMap(i -> ss.get().peek(r -> r.addLiteral(property, offset + i))));
	}

	public static <T extends Resource> Function<Supplier<Stream<T>>, Supplier<Stream<T>>>
		repeat(int n)
	{
		return (ss) -> (() -> LongStream.range(0, n).boxed()
			.flatMap(i -> ss.get()));
	}

	public static <T extends Resource, O> Function<Supplier<Stream<T>>, Supplier<Stream<T>>>
		repeatForLiterals(Property p, Stream<O> os)
	{
		return (ss) -> (() -> os.flatMap(o -> ss.get().peek(r -> r.addLiteral(p, o))));
	}


	public static <I extends Resource, O extends Resource> Function<Supplier<Stream<I>>, Supplier<Stream<O>>>
		map(Function<I, O> fn)
	{
		return (ss) -> (() -> ss.get().map(fn));
	}

	public static <I extends Resource, O extends Resource>
	Function<Supplier<Stream<I>>, Supplier<Stream<O>>>
		flatMap(Function<I, Stream<O>> fn)
	{
		return (ss) -> (() -> ss.get().flatMap(fn));
	}


	public static <T extends Resource> Function<Supplier<Stream<T>>, Supplier<Stream<T>>>
		peek(Consumer<T> consumer)
	{
		return (ss) -> (() -> ss.get().peek(consumer));
	}

	public static <T extends Resource> Function<Supplier<Stream<T>>, Supplier<Stream<T>>>
		filter(Predicate<T> predicate)
	{
		return (ss) -> (() -> ss.get().filter(predicate));
	}


	/**
	 * Sequentially perform sub-flows
	 * by passing a given streamSupplier to each subFlow in sequence.
	 *
	 * Useful e.g. for warm-up runs
	 *
	 * .seq(
	 *   start().setAttr(r -> r.setProperty(WARMUP, true).run(...).repeat(...), // first flow
	 *   execMeasurement(...).repeat(...), // second flow
	 * ).filter(r.getProperty(WARMUP) == null)
	 * ...
	 *
	 *
	 *
	 *
	 *
	 * @param subFlows
	 * @return
	 */
//	@SafeVarargs
//	public static <T extends Resource, O extends Resource> Function<Supplier<Stream<T>>, Supplier<Stream<O>>>
//		seq(Function<Supplier<Stream<T>>, Supplier<Stream<O>>> ... subFlows)
//	{
//		return (ss) -> (() -> Stream.of(subFlows).flatMap(subFlow -> subFlow.apply(ss).get()));
//	}

	@SafeVarargs
	public static <I extends Resource, O extends Resource>
	Function<Supplier<Stream<I>>, Supplier<Stream<O>>>
		seq(Function<Supplier<Stream<I>>, Supplier<Stream<O>>> ... subFlows)
	{
		return (ss) -> (() -> Stream.of(subFlows).flatMap(subFlow -> subFlow.apply(ss).get()));
	}

//	public static <T extends Resource, O extends Resource> Function<Supplier<Stream<T>>, Supplier<Stream<O>>>
//		seq(Iterable<Function<Supplier<Stream<T>>, Supplier<Stream<O>>>> subFlows)
//	{
//		return (ss) -> (() -> StreamUtils.stream(subFlows).flatMap(subFlow -> subFlow.apply(ss).get()));
//	}

	/**
	 * withIndex creates a new stream, with each resource having an incremented value for the given property.
	 *
	 * @param p
	 * @return
	 */
	public static <T extends Resource> Function<Supplier<Stream<T>>, Supplier<Stream<T>>>
		withIndex(Property p)
	{
		return (ss) -> (() -> {
			int i[] = {0};
			return ss.get().peek(r -> r.addLiteral(p, new Integer(++i[0])));
		});
	}
}
