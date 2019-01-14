package org.aksw.jena_sparql_api.core.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.function.Function;

import org.aksw.jena_sparql_api.utils.IteratorClosable;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.util.iterator.ClosableIterator;

import com.github.davidmoten.rx2.flowable.Transformers;

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
					return new IteratorClosable<>(it, new Closeable() {
						@Override
						public void close() throws IOException {
							// FIXME Looks like Jena's producer thread can get block when parsed items are not consumed
							// So we have to consume the iterator for now
							try {
								while(it.hasNext()) {
									it.next();
								}
							} finally {
								
								// Try to close the iterator 'it'
								// Otherwise, forcefully close the stream
								// (may cause a (usually/hopefully) harmless exception)
								try {
									if(it instanceof Closeable) {
							            ((Closeable)it).close();
									} else if (it instanceof org.apache.jena.atlas.lib.Closeable) {
							            ((org.apache.jena.atlas.lib.Closeable)it).close();								
									}
								} finally {
									// Close the backing input stream in any case
									in.close();
								}
							}
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
}
