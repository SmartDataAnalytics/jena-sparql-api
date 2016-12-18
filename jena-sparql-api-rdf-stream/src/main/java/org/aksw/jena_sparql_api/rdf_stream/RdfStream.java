package org.aksw.jena_sparql_api.rdf_stream;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.LongStream;
import java.util.stream.Stream;

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
	public static <T extends Resource> Function<Supplier<Stream<T>>, Supplier<Stream<ResourceEnh>>> start() {
		return (ss) -> (() -> ss.get().map(RdfStream::copyResourceClosureIntoModelEnh));
	}

	public static <T extends Resource> Supplier<Stream<T>> withIndex(Supplier<Stream<T>> stream, Property p) {
		int i = 0;

		return () -> stream.get().peek(r -> r.addLiteral(p, new Integer(i)));
	}

//		public static <T extends Resource> Supplier<Stream<T>> repeat(Supplier<Stream<T>> stream, int n) {
//			int i = 0;
//
//			return () -> LongStream.range(0, n).boxed().flatMap(x -> stream.get());
//		}

	public static <T extends Resource> Function<Supplier<Stream<T>>, Supplier<Stream<T>>> repeat(int n) {
		//return (ss) -> Stream.generate(ss).limit(n); //LongStream.range(0, n).boxed().map(i -> () -> ss.get().stream());
		return (ss) -> (() -> LongStream.range(0, n).boxed().flatMap(i -> ss.get()));
	}

	public static <T extends Resource, O> Function<Supplier<Stream<T>>, Supplier<Stream<T>>> repeat(Property p, Stream<O> os) {
		//return (ss) -> Stream.generate(ss).limit(n); //LongStream.range(0, n).boxed().map(i -> () -> ss.get().stream());
		return (ss) -> (() -> os.flatMap(o -> ss.get().peek(r -> r.addLiteral(p, o))));
	}


	public static <I extends Resource, O extends Resource> Function<Supplier<Stream<I>>, Supplier<Stream<O>>> map(Function<I, O> fn) {
		return (ss) -> (() -> ss.get().map(fn));
	}

	public static <T extends Resource> Function<Supplier<Stream<T>>, Supplier<Stream<T>>> peek(Consumer<T> consumer) {
		return (ss) -> (() -> ss.get().peek(consumer));
	}

	public static <T extends Resource> Function<Supplier<Stream<T>>, Supplier<Stream<T>>> withIndex(Property p) {
		return (ss) -> (() -> {
			int i[] = {0};
			return ss.get().peek(r -> r.addLiteral(p, new Integer(++i[0])));
		});
	}
}
