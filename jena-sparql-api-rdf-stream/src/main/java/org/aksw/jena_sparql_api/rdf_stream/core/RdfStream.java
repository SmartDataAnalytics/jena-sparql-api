package org.aksw.jena_sparql_api.rdf_stream.core;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.rdf_stream.enhanced.ModelFactoryEnh;
import org.aksw.jena_sparql_api.rdf_stream.enhanced.ResourceEnh;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.ResourceUtils;

public class RdfStream {
    public static ResourceEnh copyResourceClosureIntoModelEnh(Resource task) {
		Model m = ModelFactoryEnh.createModel();
		m.add(ResourceUtils.reachableClosure(task));
		ResourceEnh result = task.inModel(m).as(ResourceEnh.class);
		return result;
    }

	/**
	 * starts with copyResourceClosureIntoModelEnh
	 *
	 * For each item in the stream, map its closure to a new resource
	 * @param stream
	 * @param p
	 * @return
	 */
	public static <T extends Resource> Function<Supplier<Stream<T>>, Supplier<Stream<ResourceEnh>>>
		start()
	{
		return (ss) -> (() -> ss.get().map(RdfStream::copyResourceClosureIntoModelEnh));
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
		repeat(Property p, Stream<O> os)
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
		//return (ss) -> (() -> ss.get().flatMap(x -> fn.apply(x)));
		return (ss) -> (() -> ss.get().flatMap(fn));
	}


	public static <T extends Resource> Function<Supplier<Stream<T>>, Supplier<Stream<T>>>
		peek(Consumer<T> consumer)
	{
		return (ss) -> (() -> ss.get().peek(consumer));
	}


//	public static <T extends Resource> Function<Supplier<Stream<T>>, Supplier<Stream<T>>>
//		seq(Supplier<Stream<T>> ... subFlows)
//	{
//		return (ss) -> (() -> ss.get().peek(consumer));
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
