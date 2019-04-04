package org.aksw.jena_sparql_api.core.utils;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.utils.DatasetUtils;
import org.aksw.jena_sparql_api.utils.IteratorClosable;
import org.aksw.jena_sparql_api.utils.QuadPatternUtils;
import org.apache.jena.ext.com.google.common.collect.Iterators;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.DatasetImpl;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.util.iterator.ClosableIterator;

import com.github.davidmoten.rx2.flowable.Transformers;
import com.google.common.collect.Lists;

import io.reactivex.Flowable;

/**
 * Reactive extensions of RDFDataMgr
 * 
 * @author Claus Stadler, Nov 12, 2018
 *
 */
public class RDFDataMgrRx {

	public static Flowable<Quad> createFlowableQuads(Callable<InputStream> inSupplier, Lang lang, String baseIRI) {
		return createFlowableFromInputStream(inSupplier, in -> RDFDataMgr.createIteratorQuads(in, lang, baseIRI));
	}

	public static Flowable<Triple> createFlowableTriples(Callable<InputStream> inSupplier, Lang lang, String baseIRI) {
		return createFlowableFromInputStream(inSupplier, in -> RDFDataMgr.createIteratorTriples(in, lang, baseIRI));
	}

	
	/**
	 * Creates resources by grouping consecutive quads with the same graph into a Model,
	 * and then returning a resource for that graph IRI.
	 * 
	 * {@code
	 * GRAPH :s {
	 *   :s :p :o .
	 *   :o :x :y
	 * }
	 * }
	 * 
	 * 
	 * @param inSupplier
	 * @param lang
	 * @param baseIRI
	 * @return
	 */
	public static Flowable<Resource> createFlowableResources(Callable<InputStream> inSupplier, Lang lang, String baseIRI) {
		Flowable<Resource> result = createFlowableQuads(inSupplier, lang, baseIRI)	
			.compose(Transformers.<Quad>toListWhile(
		            (list, t) -> list.isEmpty() 
		                         || list.get(0).getGraph().equals(t.getGraph())))
			.map(list -> list.stream().map(RDFDataMgrRx::decodeDistinguished).collect(Collectors.toList()))
			.map(QuadPatternUtils::createResourceFromQuads);

		return result;
	}
	
	public static final String DISTINGUISHED_PREFIX = "distinguished://";
	public static final int DISTINGUISHED_PREFIX_LENGTH = DISTINGUISHED_PREFIX.length();
	
	public static Node encodeDistinguished(Node g) {
		Node result = g;
		if(g.isURI()) {
			String str = DISTINGUISHED_PREFIX + g.getURI();
			result = NodeFactory.createURI(str);
		}
		return result;
	}

	public static Quad encodeDistinguished(Quad quad) {
		Node g = quad.getGraph();
		Node encoded = encodeDistinguished(g);
		Quad result = encoded == g ? quad : new Quad(encoded, quad.asTriple());

		return result;
	}

	public static Node decodeDistinguished(Node g) {
		Node result = g;
		if(g.isURI()) {
			String str = g.getURI();
			if(str.startsWith(DISTINGUISHED_PREFIX)) {
				result = NodeFactory.createURI(str.substring(DISTINGUISHED_PREFIX_LENGTH));
			}
		}
		return result;
	}
	
	public static Quad decodeDistinguished(Quad quad) {
		Quad result = quad;
		Node g = quad.getGraph();
		if(g.isURI()) {
			String str = g.getURI();
			if(str.startsWith(DISTINGUISHED_PREFIX)) {
				result = new Quad(NodeFactory.createURI(str.substring(DISTINGUISHED_PREFIX_LENGTH)), quad.asTriple());
			}
		}
		return result;
	}
	
	/**
	 * Groups consecutive quads with the same graph yeld by createFlowableQuads into datasets 
	 * 
	 * @param inSupplier
	 * @param lang
	 * @param baseIRI
	 * @return
	 */
	public static Flowable<Dataset> createFlowableDatasets(Callable<InputStream> inSupplier, Lang lang, String baseIRI) {
		Flowable<Dataset> result = createFlowableQuads(inSupplier, lang, baseIRI)		
			.compose(Transformers.<Quad>toListWhile(
		            (list, t) -> list.isEmpty() 
		                         || list.get(0).getGraph().equals(t.getGraph())))
			.map(DatasetGraphQuadsImpl::create)
			.map(DatasetFactory::wrap);

		return result;
	}
	
	
	public static <T> Flowable<T> createFlowableFromInputStream(Callable<? extends InputStream> inSupplier, Function<? super InputStream, ? extends Iterator<T>> fn) {
		Flowable<T> result = Flowable.generate(
				() -> {
					InputStream in = inSupplier.call();
					Iterator<T> it = fn.apply(in);
					return new IteratorClosable<>(it, () -> {
						// Try to close the iterator 'it'
						// Otherwise, forcefully close the stream
						// (may cause a (usually/hopefully) harmless exception)
						try {
							if(it instanceof Closeable) {
					            ((Closeable)it).close();
							} else if (it instanceof org.apache.jena.atlas.lib.Closeable) {
					            ((org.apache.jena.atlas.lib.Closeable)it).close();								
							} else {
								try {
									in.close();
								} finally {
									// Consume any remaining items in the iterator to prevent blocking issues
									// For example, Jena's producer thread can get blocked
									// when parsed items are not consumed
									Iterators.size(it);
								}
							}
						} finally {
							// Close the backing input stream in any case
							in.close();
						}
					});
				},
				(reader, emitter) -> {
					if(reader.hasNext()) {
						T item = reader.next();
						emitter.onNext(item);
					} else {
						emitter.onComplete();
					}
				},
				ClosableIterator::close);
		return result;
	}

	public static void writeResources(Flowable<? extends Resource> flowable, Path file, RDFFormat format) throws Exception {
		writeDatasets(flowable.map(DatasetUtils::createFromResource), file, format);
	}

	
	
//	static class State {
//		Path targetPath;
//		Path tmpPath;
//		OutputStream out;
//	}
//	interface Sink {
//		
//	}
//	
//    public static <T> FlowableTransformer<T, T> cache(Path file, BiConsumer<T, OutputStream> serializer) {
//    	
//    	State[] state = {null};
//    	return f -> f
//    			.doOnSubscribe(s -> state[0] = new State())
//    			.doOnNext(item -> serializer.accept(state[0].out, item))
//    			.doOnCancel(onCancel)
//    		;
//    	
//    	
////    	return f ->
////          f.doOnSubscrible // open the stream
////    		f.doOnNext() // write the item
////    	    f.onCancel() // delete the file
////          f.onError // delete the file
////    	    f.onComplete // close the stream, copy the file to the final location
//    		
////            final BiPredicate<? super List<T>, ? super T> condition, boolean emitRemainder) {
////        return collectWhile(ListFactoryHolder.<T>factory(), ListFactoryHolder.<T>add(), condition, emitRemainder);
//    }
	
	
	/**
	 * An encoder that renames a graph if it is encountered on successive encoding requests.
	 * 
	 * @author raven
	 *
	 */
	public static class QuadEncoderDistinguish {
		protected Set<Node> priorGraphs = Collections.emptySet();

		public synchronized Dataset encode(Dataset dataset) {
			Set<Node> now = Sets.newHashSet(dataset.asDatasetGraph().listGraphNodes());
			List<Quad> quads = Lists.newArrayList(dataset.asDatasetGraph().find());
			
			Set<Node> conflicts = Sets.intersection(priorGraphs, now);
			List<Quad> newQuads = quads.stream()
					.map(q -> conflicts.contains(q.getGraph()) ? encodeDistinguished(q) : q)
					.collect(Collectors.toList());
			
			priorGraphs = now.stream()
				.map(n -> conflicts.contains(n) ? encodeDistinguished(n) : n)
				.collect(Collectors.toSet());
			
			Dataset result = DatasetFactory.wrap(DatasetGraphQuadsImpl.create(newQuads));
			return result;
			// Rename all graphs in the intersection
		}
	}

	// A better approach would be to transform a flowable to write to a file as a side effect
	// Upon flowable completion, copy the file to its final location
	public static void writeDatasets(Flowable<? extends Dataset> flowable, Path file, RDFFormat format) throws Exception {
		try(FileOutputStream out = new FileOutputStream(file.toFile())) {
			try {
				QuadEncoderDistinguish encoder = new QuadEncoderDistinguish();
				flowable.blockingForEach(d -> RDFDataMgr.write(out, encoder.encode(d), format));
			} finally {
				out.flush();
				out.close();				
			}
		}
	}

}
