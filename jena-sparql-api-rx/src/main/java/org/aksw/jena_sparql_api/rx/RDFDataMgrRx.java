package org.aksw.jena_sparql_api.rx;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.rx.op.FlowOfQuadsOps;
import org.aksw.jena_sparql_api.utils.DatasetGraphUtils;
import org.aksw.jena_sparql_api.utils.DatasetUtils;
import org.aksw.jena_sparql_api.utils.QuadPatternUtils;
import org.aksw.jena_sparql_api.utils.QuadUtils;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.ext.com.google.common.base.Predicate;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.irix.IRIxResolver;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RIOT;
import org.apache.jena.riot.ResultSetMgr;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.RiotParseException;
import org.apache.jena.riot.lang.LabelToNode;
import org.apache.jena.riot.lang.PipedQuadsStream;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedTriplesStream;
import org.apache.jena.riot.lang.RiotParsers;
import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.riot.system.FactoryRDF;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.ParserProfileStd;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWrapper;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.util.Context;

// import com.github.davidmoten.rx2.flowable.Transformers;
import com.google.common.collect.Lists;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableTransformer;
import io.reactivex.rxjava3.core.Maybe;

/**
 * Reactive extensions of RDFDataMgr
 *
 * @author Claus Stadler, Nov 12, 2018
 *
 */
public class RDFDataMgrRx {

    /**
     * Helper interface to configure a factory that creates a function that turns an InputStream
     * into an iterator.
     *
     * INPUT may be a TypedInputStream (tis) which is an input stream but also holds metadata.
     * This leads to code where the same tis is supplied twice: once in the role of configuration metadata
     * and once as the actual input stream:
     * Iterator&lt;T&gt; it = apply(th, eh, tis).apply(tis)
     *
     * @author raven
     *
     * @param <INPUT>
     * @param <T> The type of items to deserialize from the input stream based on INPUT
     */
    public static interface IteratorFactoryFactory<INPUT, T> {
        Function<InputStream, Iterator<T>> apply(Consumer<Thread> th, UncaughtExceptionHandler eh, INPUT input);
    }


    public static Flowable<Triple> createFlowableTriples(String filenameOrURI, Lang lang, String baseIRI) {
        return createFlowableTriples(() -> RDFDataMgr.open(filenameOrURI), lang, baseIRI);
    }


    /**
     * Create a Flowable for a SPARQL result set backed by a file
     *
     * @param filenameOrURI
     * @param lang
     * @return
     */
    public static Flowable<Binding> createFlowableBindings(String filenameOrURI, Lang lang) {
        return createFlowableBindings(() -> RDFDataMgr.open(filenameOrURI), lang);
    }

    /**
     * Create a Flowable for a SPARQL result set backed by an supplier of input streams
     *
     * @param filenameOrURI
     * @param lang
     * @return
     */
    public static Flowable<Binding> createFlowableBindings(Callable<InputStream> inSupp, Lang lang) {
        return createFlowableBindings(() -> {
            ContentType ct = lang.getContentType();
            InputStream in = inSupp.call();
            return new TypedInputStream(in, ct);
        });
    }


    /**
     * Create a Flowable for a SPARQL result set backed by a supplier of TypedInputStream
     *
     * @param filenameOrURI
     * @param lang
     * @return
     */
    public static Flowable<Binding> createFlowableBindings(Callable<TypedInputStream> inSupp) {
        Flowable<Binding> result = createFlowableFromResource(
                inSupp,
                in -> {
                    Lang lang = RDFLanguages.contentTypeToLang(in.getContentType());
                    ResultSet rs = ResultSetMgr.read(in.getInputStream(), lang);
                    return rs;
                },
                ResultSet::hasNext,
                ResultSet::nextBinding,
                in -> { try { in.close(); } catch (Exception e) { throw new RuntimeException(e); } }
            );

        return result;
    }


    /**
     * Generic helper to create a Flowable by mapping some resource such as in InputStream or
     * a QueryExecution to an iterable such as an ResultSet
     *
     * @param <R>
     * @param <I>
     * @param <T>
     * @param resourceSupplier
     * @param resourceToIterator
     * @param hasNext
     * @param next
     * @param closeResource
     * @return
     */
    public static <R, I, T> Flowable<T> createFlowableFromResource(
            Callable<R> resourceSupplier,
            Function<? super R, I> resourceToIterator,
            Predicate<? super I> hasNext,
            Function<? super I, T> next,
            Consumer<? super R> closeResource) {

        Flowable<T> result = Flowable.generate(
                () -> {
                    R in = resourceSupplier.call();
                    return new SimpleEntry<R, I>(in, null);
                },
                (state, emitter) -> {
                    I it = state.getValue();

                    try {
                        if (it == null) {
                            R in = state.getKey();
                            it = resourceToIterator.apply(in);
                            state.setValue(it);
                        }

                        boolean hasMore = hasNext.apply(it);
                        if (hasMore) {
                            T value = next.apply(it);
                            emitter.onNext(value);
                        } else {
                            emitter.onComplete();
                        }
                    } catch (Exception e) {
                        emitter.onError(e);
                    }
                },
                state -> {
                    R in = state.getKey();
                    if (in != null) {
                        closeResource.accept(in);
                    }
                });

        return result;
    }

    public static Flowable<Triple> createFlowableTriples(Callable<InputStream> inSupplier, Lang lang, String baseIRI) {
        return createFlowableFromInputStream(
                inSupplier,
                (th, eh, rawIn) -> (in -> createIteratorTriples(in, lang, baseIRI, eh, th)));
    }

    public static Flowable<Resource> createFlowableResources(String filenameOrURI, Lang lang, String baseIRI) {
        return createFlowableResources(() -> RDFDataMgr.open(filenameOrURI), lang, baseIRI);
    }

    public static Flowable<Dataset> createFlowableDatasets(String filenameOrURI, Lang lang, String baseIRI) {
        return createFlowableDatasets(() -> RDFDataMgr.open(filenameOrURI), lang, baseIRI);
    }


    public static RDFIterator<Quad> createIteratorQuads(
            InputStream in,
            Lang lang,
            String baseIRI,
            UncaughtExceptionHandler eh,
            Consumer<Thread> th) {
        return createIteratorQuads(
                in,
                lang,//RDFLanguages.contentTypeToLang(in.getContentType()),
                baseIRI,
                PipedRDFIterator.DEFAULT_BUFFER_SIZE,
                false,
                PipedRDFIterator.DEFAULT_POLL_TIMEOUT,
                Integer.MAX_VALUE,
                eh,
                th);
    }

    public static RDFIterator<Quad> createIteratorQuads(
            TypedInputStream in,
            UncaughtExceptionHandler eh,
            Consumer<Thread> th) {
        return createIteratorQuads(
                in,
                RDFLanguages.contentTypeToLang(in.getContentType()),
                in.getBaseURI(),
                eh,
                th);
    }

    public static RDFIterator<Triple> createIteratorTriples(
            InputStream in,
            Lang lang,
            String baseIRI,
            UncaughtExceptionHandler eh,
            Consumer<Thread> threadHandler) {
        return createIteratorTriples(
                in,
                lang,//RDFLanguages.contentTypeToLang(in.getContentType()),
                baseIRI,
                PipedRDFIterator.DEFAULT_BUFFER_SIZE,
                false,
                PipedRDFIterator.DEFAULT_POLL_TIMEOUT,
                Integer.MAX_VALUE,
                threadHandler,
                eh);
    }

    public static RDFIterator<Triple> createIteratorTriples(
            TypedInputStream in,
            UncaughtExceptionHandler eh,
            Consumer<Thread> th) {
        return createIteratorTriples(
                in,
                RDFLanguages.contentTypeToLang(in.getContentType()),
                in.getBaseURI(),
                eh,
                th);
    }


    /**
     * Adaption from RDFDataMgr.createIteratorQuads that waits for
     * data on the input stream indefinitely and allows for thread handling
     *
     * Creates an iterator over parsing of quads
     * Upgrades triples to quads with graph set to Quad.defaultGraphNodeGenerated if lang refers to a triple language
     *
     * @param input Input Stream
     * @param lang Language
     * @param baseIRI Base IRI
     * @return Iterator over the quads
     */
    public static RDFIterator<Quad> createIteratorQuads(
            InputStream input,
            Lang lang,
            String baseIRI,
            int bufferSize, boolean fair, int pollTimeout, int maxPolls,
            UncaughtExceptionHandler eh,
            Consumer<Thread> th) {

        // Special case N-Quads, because the RIOT reader has a pull interface
        if ( RDFLanguages.sameLang(RDFLanguages.NQUADS, lang) ) {
            return new RDFIteratorFromIterator<Quad>(Iter.onCloseIO(
                RiotParsers.createIteratorNQuads(input, null, RDFDataMgrRx.dftProfile()),
                input), baseIRI);
        }
        // Otherwise, we have to spin up a thread to deal with it
        RDFIteratorFromPipedRDFIterator<Quad> it = new RDFIteratorFromPipedRDFIterator<>(bufferSize, fair, pollTimeout, maxPolls);

        // Upgrade triples to quads; this happens if quads are requested from a triple lang
        PipedQuadsStream out = new PipedQuadsStream(it) {
            @Override
            public void triple(Triple triple) {
                Quad q = new Quad(Quad.defaultGraphNodeGenerated, triple);
                quad(q);
            }
        };

        // We need to handle finish ourself in order to pass any raised exception to rxjava
        StreamRDF ignoreFinishWrapper = new StreamRDFWrapper(out) {
            @Override public void finish() {}
        };


        Thread t = new Thread(() -> {
            try {
                // Invoke start on the sink so that the consumer knows the producer thread
                // It appears otherwise the producer thread can get interrupted before
                // the consumer gets to know the producer (which happens on start)
                out.start();
                parseFromInputStream(ignoreFinishWrapper, input, baseIRI, lang, null);
            } catch(Exception e) {
                // Ensure the exception handler is run before any
                // thread.join() waiting for this thread
                eh.uncaughtException(Thread.currentThread(), e);
            } finally {
                try {
                    out.finish();
                } catch (Exception e2) {
                    // Silently ignore failure on finish due to closed consumer
                }
            }
        });
        th.accept(t);
        t.start();
        return it;
    }


    /**
     * Label to node strategy that passes existing labels on as given
     * but allocation of fresh nodes uses a pair comprising a jvm-global random value and an increment.
     * (i.e. incremental numbers scoped within some random value)
     *
     * This strategy is needed when processing RDF files in splits such as with Apache Spark:
     * Any mentioned labels should be retaine globally, but fresh nodes allocated for the splits must not clash.
     *
     * @return
     */
    public static LabelToNode createLabelToNodeAsGivenOrRandom() {
        return new LabelToNode(
                new AllocScopePolicy(),
                new Alloc(BlankNodeAllocatorAsGivenOrRandom.getGlobalInstance()));
    }


    public static ErrorHandler dftErrorHandler() {
        return ErrorHandlerFactory.errorHandlerWarn;
    }

    public static ParserProfile dftProfile() {
        return permissiveProfile();
    }

    public static ParserProfile createParserProfile(FactoryRDF factory, ErrorHandler errorHandler, boolean checking) {
        return new ParserProfileStd(factory,
                                    errorHandler,
                                    // IRIxResolver.create(IRIs.getSystemBase()).build(),
                                    IRIxResolver.create().noBase().allowRelative(true).build(),
                                    PrefixMapFactory.create(),
                                    RIOT.getContext().copy(),
                                    checking,
                                    false);
    }

    public static ParserProfile strictProfile() {
        return createParserProfile(RiotLib.factoryRDF(
                createLabelToNodeAsGivenOrRandom()),
                ErrorHandlerFactory.errorHandlerDetailed(),
                true);
    }

    public static ParserProfile permissiveProfile() {
        return createParserProfile(RiotLib.factoryRDF(
                createLabelToNodeAsGivenOrRandom()),
                ErrorHandlerFactory.errorHandlerWarn,
                false);
    }

    /**
     * Adaption from RDFDataMgr.createIteratorQuads that waits for
     * data on the input stream indefinitely and allows for thread handling
     *
     * Creates an iterator over parsing of quads
     * @param input Input Stream
     * @param lang Language
     * @param baseIRI Base IRI
     * @return Iterator over the quads
     */
    public static RDFIterator<Triple> createIteratorTriples(
            InputStream input,
            Lang lang,
            String baseIRI,
            int bufferSize, boolean fair, int pollTimeout, int maxPolls,
            Consumer<Thread> th,
            UncaughtExceptionHandler eh) {
        // Special case N-Quads, because the RIOT reader has a pull interface
        if ( RDFLanguages.sameLang(RDFLanguages.NTRIPLES, lang) ) {
            return new RDFIteratorFromIterator<Triple>(Iter.onCloseIO(
                RiotParsers.createIteratorNTriples(input, null, RDFDataMgrRx.dftProfile()),
                input), baseIRI);
        }
        // Otherwise, we have to spin up a thread to deal with it
        RDFIteratorFromPipedRDFIterator<Triple> it = new RDFIteratorFromPipedRDFIterator<>(bufferSize, fair, pollTimeout, maxPolls);
        PipedTriplesStream out = new PipedTriplesStream(it);

        // We need to handle finish ourself in order to pass any raised exception to rxjava
        StreamRDF ignoreFinishWrapper = new StreamRDFWrapper(out) {
            @Override public void finish() {}
        };

        Thread t = new Thread(()-> {
            try {
                // Invoke start on the sink so that the consumer knows the producer thread
                // It appears otherwise the producer thread can get interrupted before
                // the consumer gets to know the producer (which happens on start)
                out.start();
                parseFromInputStream(ignoreFinishWrapper, input, baseIRI, lang, null);
            } catch(Exception e) {
                // Ensure the exception handler is run before any
                // thread.join() waiting for this thread
                eh.uncaughtException(Thread.currentThread(), e);
            } finally {
                try {
                    out.finish();
                } catch (Exception e2) {
                    // Silently ignore failure on finish due to closed consumer
                }
            }
        });
        th.accept(t);
        t.start();
        return it;
    }

    public static void parseFromInputStream(StreamRDF destination, InputStream in, String baseUri, Lang lang, Context context) {
        RDFParser.create()
            .source(in)
            // Disabling checking does not seem to give a significant performance gain
            // For a 3GB Trig file parsing took ~1:45 min +- 5 seconds either way
            //.checking(false)
            .base(baseUri)
            .lang(lang)
            .context(context)
            .errorHandler(dftErrorHandler())
            .labelToNode(createLabelToNodeAsGivenOrRandom())
            //.errorHandler(handler)
            .parse(destination);
    }

    public static Flowable<Quad> createFlowableQuads(String filenameOrURI, Lang lang, String baseIRI) {
        return createFlowableQuads(() -> RDFDataMgr.open(filenameOrURI), lang, baseIRI);
    }


    public static Flowable<Quad> createFlowableQuads(Callable<InputStream> inSupplier, Lang lang, String baseIRI) {
        return createFlowableFromInputStream(
                    inSupplier,
                    (th, eh, rawIn) -> (in -> createIteratorQuads(in, lang, baseIRI, eh, th)))
                // Ensure that the graph node is always non-null
                // Trig parser in Jena 3.14.0 creates quads with null graph
                .map(q -> q.getGraph() != null
                    ? q
                    : Quad.create(Quad.defaultGraphNodeGenerated, q.asTriple()));
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
//            .compose(Transformers.<Quad>toListWhile(
//                    (list, t) -> list.isEmpty()
//                                 || list.get(0).getGraph().equals(t.getGraph())))
            .compose(FlowOfQuadsOps.groupToList())
            .map(Entry::getValue)
            .map(list -> list.stream().map(RDFDataMgrRx::decodeDistinguished)
            .collect(Collectors.toList()))
            .map(QuadPatternUtils::createResourceFromQuads);

        return result;
    }


    /**
     * Prefixes to distinguishes consecutive different events of the same named graph
     *
     */
    public static final String DISTINGUISHED_PREFIX = "x-distinguished:";
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
            .compose(FlowOfQuadsOps.datasetsFromConsecutiveQuads(
                    Quad::getGraph,
                    DatasetGraphFactoryEx::createInsertOrderPreservingDatasetGraph))
            ;

        return result;
    }

    public static Flowable<Dataset> createFlowableDatasets(Callable<TypedInputStream> inSupplier) {

//        Flowable<Dataset> result = createFlowableFromInputStream(
//                inSupplier,
//                th -> eh -> in -> createIteratorQuads(
//                        in,
//                        RDFLanguages.contentTypeToLang(in.getContentType()),
//                        in.getBaseURI(),
//                        eh,
//                        th))
        Flowable<Dataset> result = createFlowableQuads(inSupplier)
                .compose(FlowOfQuadsOps.datasetsFromConsecutiveQuads(
                        Quad::getGraph,
                        DatasetGraphFactoryEx::createInsertOrderPreservingDatasetGraph))
                ;
//        .compose(Transformers.<Quad>toListWhile(
//                (list, t) -> list.isEmpty()
//                             || list.get(0).getGraph().equals(t.getGraph())))
//        .compose(DatasetGraphOpsRx.groupToList())
//        .map(Entry::getValue)
//        .map(DatasetFactoryEx::createInsertOrderPreservingDataset);

        return result;
    }

    public static Flowable<Quad> createFlowableQuads(Callable<TypedInputStream> inSupplier) {

        Flowable<Quad> result = createFlowableFromInputStream(
                inSupplier,
                (th, eh, rawIn) -> (in -> createIteratorQuads(
                        in,
                        RDFLanguages.contentTypeToLang(rawIn.getContentType()),
                        rawIn.getBaseURI(),
                        eh,
                        th)));

        return result;
    }


    public static Flowable<Triple> createFlowableTriples(Callable<TypedInputStream> inSupplier) {

        Flowable<Triple> result = createFlowableFromInputStream(
                inSupplier,
                (th, eh, rawIn) -> (in -> createIteratorTriples(
                        in,
                        RDFLanguages.contentTypeToLang(rawIn.getContentType()),
                        rawIn.getBaseURI(),
                        eh,
                        th)));

        return result;
    }


    public static <T, I extends InputStream> Flowable<T> createFlowableFromInputStream(
            Callable<I> inSupplier,
            IteratorFactoryFactory<I, T> iff) {

        // In case the creation of the iterator from an inputstream involves a thread
        // perform setup of the exception handler

        // If there is a thread, we join on it before completing the flowable in order to
        // capture any possible error

        Flowable<T> result = Flowable.generate(
                () -> {
                    FlowState<T> state = new FlowState<>();
                    I rawIn = inSupplier.call();
                    // Closing the flowable may interrupt threads which may cause
                    // unwanted close of the underlying input stream
                    // state.in = Channels.newInputStream(new ReadableByteChannelWithoutCloseOnInterrupt(rawIn));

                    state.setIn(rawIn);
                    state.setIterator(iff.apply(state::setProducerThread, state::handleProducerException, rawIn).apply(state.in));


                    // state.reader = fn.apply(state::setProducerThread).apply(state::handleException).apply(state.in);

                    return state;
                },
                (state, emitter) -> {
                    try {
                        boolean hasNext;
                        boolean isCancelled = false;

                        try {
                            hasNext = state.iterator.hasNext();
                        } catch (CancellationException | RiotException e) {
                            // RiotException is assumed to be "Producer dead"
                            hasNext = false;
                            isCancelled = true;
                        }

//						System.out.println("Generator invoked");
                        if (hasNext && !state.closeInvoked) {
//							System.out.println("hasNext = true");
                            T item = state.iterator.next();
                            emitter.onNext(item);
                        } else {
//							System.out.println("hasNext = false; Waiting for any pending exceptions from producer thread");
                            if (state.producerThread != null && !isCancelled) {
                                state.producerThread.join();
                            }
//							System.out.println("End");

                            Throwable t = state.raisedException;
                            boolean report = true;
                            if (t != null) {
                                boolean isParseError = t instanceof RiotParseException;

                                // Parse errors after an invocation of close are ignored
                                // I.e. if we asked for 5 items, and there is parse error at the 6th one,
                                // we still complete the original request without errors
                                if (isParseError && state.closeInvoked) {
                                    report = false;
                                }
                            }

                            if (t != null && report) {
                                emitter.onError(state.raisedException);
                            } else {
                                emitter.onComplete();
                            }
                        }
                    } catch(Exception e) {
                        emitter.onError(e);
                    }
                },
                state -> state.close());
        return result;
    }

    public static void writeResources(Flowable<? extends Resource> flowable, Path file, RDFFormat format) throws Exception {
        writeDatasets(flowable.map(DatasetUtils::createFromResource), file, format);
    }

    public static void writeResources(Flowable<? extends Resource> flowable, OutputStream out, RDFFormat format) throws Exception {
        writeDatasets(flowable.map(DatasetUtils::createFromResource), out, format);
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

        // Do we need synchronized? Processing should happen in order anyway!
        public Dataset encode(Dataset dataset) {
            Set<Node> now = Sets.newHashSet(dataset.asDatasetGraph().listGraphNodes());
            List<Quad> quads = Lists.newArrayList(dataset.asDatasetGraph().find());

            Set<Node> conflicts = Sets.intersection(priorGraphs, now);
            List<Quad> newQuads = quads.stream()
                    .map(q -> conflicts.contains(q.getGraph()) ? encodeDistinguished(q) : q)
                    .collect(Collectors.toList());

            priorGraphs = now.stream()
                .map(n -> conflicts.contains(n) ? encodeDistinguished(n) : n)
                .collect(Collectors.toSet());

            Dataset result = DatasetFactoryEx.createInsertOrderPreservingDataset(newQuads);
            return result;
            // Rename all graphs in the intersection
        }
    }

    /**
     * Stateful collector that merges any consecutive graphs of name
     * contained in the datasets passed to the accept method.
     *
     *
     * @author raven
     *
     */
    public static class ConsecutiveNamedGraphMerger
        extends ConsecutiveNamedGraphMergerCore<Dataset>
    {
        @Override
        protected Dataset mapResult(Set<Node> readyGraphs, Dataset dataset) {
            return dataset;
        }
    }

    public static abstract class ConsecutiveNamedGraphMergerCore<T> {
        protected Map<Node, Set<Quad>> pending = new LinkedHashMap<>();

        public synchronized Optional<T> accept(Dataset dataset) {
            Supplier<Set<Quad>> setSupplier = LinkedHashSet::new;

            Iterator<Quad> it = dataset.asDatasetGraph().find();
            Map<Node, Set<Quad>> index = QuadUtils.partitionByGraph(
                    it,
                    new LinkedHashMap<Node, Set<Quad>>(),
                    setSupplier);

            Set<Node> before = pending.keySet();
            Set<Node> now = index.keySet();

            Set<Node> overlap = Sets.intersection(now, before);
            Set<Node> newGraphs = Sets.difference(now, before);
//					readyGraphs,
//					Sets.difference(now, before));

            for(Node appending : overlap) {
                Set<Quad> tgt = pending.get(appending);
                Set<Quad> src = index.get(appending);
                tgt.addAll(src);
            }

            for(Node newGraph : newGraphs) {
                Set<Quad> src = index.get(newGraph);
                pending.put(newGraph, src);
            }

            // Emit the ready graphs
            Dataset resultDataset = DatasetFactoryEx.createInsertOrderPreservingDataset();
            Set<Node> readyGraphs = new HashSet<>(Sets.difference(before, now));

            for(Node ready : readyGraphs) {
                Set<Quad> quads = pending.get(ready);

                DatasetGraphUtils.addAll(resultDataset.asDatasetGraph(), quads);

                pending.remove(ready);
            }

            T result = readyGraphs.isEmpty()
                    ? null
                    : mapResult(readyGraphs, resultDataset);

            //System.err.println("Pending size " + pending..size());

            return Optional.ofNullable(result);
        }

        protected abstract T mapResult(Set<Node> readyGraphs, Dataset dataset);

        public Optional<T> getPendingDataset() {
            T resultData;
            if(pending.isEmpty()) {
                resultData = null;
            } else {
                Dataset dataset = DatasetFactoryEx.createInsertOrderPreservingDataset();
                for(Collection<Quad> quads : pending.values()) {
                    DatasetGraphUtils.addAll(dataset.asDatasetGraph(), quads);
                }

                resultData = mapResult(pending.keySet(), dataset);
            }
            return Optional.ofNullable(resultData);
        }
    }




    @Deprecated
    public static class QuadEncoderMergeOld {
        protected Dataset pending = DatasetFactory.create();

        public synchronized Dataset accept(Dataset dataset) {
            Set<Node> before = Sets.newHashSet(pending.asDatasetGraph().listGraphNodes());
            Set<Node> now = Sets.newHashSet(dataset.asDatasetGraph().listGraphNodes());

            Set<Node> readyGraphs = Sets.difference(before, now);
            Set<Node> appendings = Sets.union(
                    Sets.intersection(before, now),
                    Sets.difference(now, before));

            for(Node appending : appendings) {
                Graph tgt = pending.asDatasetGraph().getGraph(appending);
                Graph src = dataset.asDatasetGraph().getGraph(appending);

                GraphUtil.addInto(tgt, src);
            }

            // Emit the ready graphs
            Dataset result = DatasetFactory.create();
            for(Node ready : readyGraphs) {
                Graph src = pending.asDatasetGraph().getGraph(ready);
                DatasetGraphUtils.addAll(result.asDatasetGraph(), ready, src);

                pending.asDatasetGraph().removeGraph(ready);
            }

            System.err.println("Pending size " + pending.asDatasetGraph().size());

            return result;
        }

        public Dataset getPendingDataset() {
            return pending;
        }
    }
    // A better approach would be to transform a flowable to write to a file as a side effect
    // Upon flowable completion, copy the file to its final location
    public static void writeDatasets(Flowable<Dataset> flowable, Path file, RDFFormat format) throws Exception {
        try(OutputStream out = new FileOutputStream(file.toFile())) {
            writeDatasets(flowable, out, format);
        }
    }

//	public Consumer<? extends Dataset> createWriter(OutputStream out, RDFFormat format, int flushSize) {
//
//	}

//    public static Consumer<Dataset> createDatasetWriter(OutputStream out, RDFFormat format) {
//        return ds -> RDFDataMgr.write(out, ds, format);
//    }

    public static FlowableTransformer<? super Dataset, Throwable> createWriterDataset(OutputStream out, RDFFormat format) {
        return upstream -> upstream
            .buffer(1)
            .compose(RDFDataMgrRx.createBatchWriterDataset(out, format));
    }

    public static <C extends Collection<? extends Dataset>> FlowableTransformer<C, Throwable> createBatchWriterDataset(OutputStream out, RDFFormat format) {
        QuadEncoderDistinguish encoder = new QuadEncoderDistinguish();
        Lang lang = format.getLang();
        boolean isLangTriples = RDFLanguages.isTriples(lang);

        // TODO Prevent emitting of redundant prefix mappings

        return upstream -> upstream
                .concatMapMaybe(batch -> {
                    for(Dataset item : batch) {
                        Dataset encoded = encoder.encode(item);

                        if(isLangTriples) {
                            Iterator<String> it = item.listNames();
                            while(it.hasNext()) {
                                String name = it.next();
                                Model m = item.getNamedModel(name);
                                RDFDataMgr.write(out, m, format);
                            }
                        } else {
                            RDFDataMgr.write(out, encoded, format);
                        }
                    }
                    out.flush();
                    return Maybe.<Throwable>empty();
                })
                .onErrorReturn(t -> t);
    }


    /**
     *
     * @param <C>
     * @param out
     * @param format Only NQuads is currently supported
     * @return
     */
    public static <C extends Collection<Quad>> FlowableTransformer<C, Throwable> createBatchWriterQuads(OutputStream out, RDFFormat format) {
        if (!Lang.NQUADS.equals(format.getLang())) {
            throw new IllegalArgumentException("Only nquads based formats are currently supported");
        }

        return upstream -> upstream
                .concatMapMaybe(batch -> {
                    RDFDataMgr.writeQuads(out, batch.iterator());
                    out.flush();
                    return Maybe.<Throwable>empty();
                })
                .onErrorReturn(t -> t);
    }

    public static void writeQuads(Flowable<Quad> flowable, OutputStream out, RDFFormat format) throws IOException {

        Flowable<Throwable> tmp = flowable
            .buffer(128)
            .compose(RDFDataMgrRx.createBatchWriterQuads(out, format));

        Throwable e = tmp.singleElement().blockingGet();
        if(e != null) {
            throw new IOException(e);
        }
    }





    /**
     *  Does not close the output stream
     *
     *  Note that you can use .createDatasetBatchWriter()
     *  ....blockingForEach(createDatasetBatchWriter())
     *
     *  This does not break the chain and gives freedom over the choice of forEach type (non-/blocking)
     */
    public static void writeDatasets(Flowable<Dataset> flowable, OutputStream out, RDFFormat format) throws IOException {

      Flowable<Throwable> tmp = flowable
          .buffer(1)
          .compose(RDFDataMgrRx.createBatchWriterDataset(out, format));

      Throwable e = tmp.singleElement().blockingGet();
      if(e != null) {
          throw new IOException(e);
      }

if(false) {
        QuadEncoderDistinguish encoder = new QuadEncoderDistinguish();
        flowable
        // Flush every n datasets
        .buffer(1)
        .forEach(items -> {
            for(Dataset item : items) {
                Dataset encoded = encoder.encode(item);
                RDFDataMgr.write(out, encoded, format);
            }
            out.flush();
        });
}


    }

}
