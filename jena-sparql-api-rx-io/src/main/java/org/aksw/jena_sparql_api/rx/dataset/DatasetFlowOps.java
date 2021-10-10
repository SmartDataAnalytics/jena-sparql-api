package org.aksw.jena_sparql_api.rx.dataset;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.aksw.commons.io.syscall.SysCalls;
import org.aksw.commons.io.syscall.sort.SysSort;
import org.aksw.commons.rx.op.RxOps;
import org.aksw.jena_sparql_api.io.json.TypeAdapterDataset;
import org.aksw.jena_sparql_api.io.json.TypeAdapterNode;
import org.aksw.jena_sparql_api.rx.DatasetFactoryEx;
import org.aksw.jena_sparql_api.rx.RDFDataMgrRx;
import org.aksw.jena_sparql_api.rx.RDFDataMgrRx.ConsecutiveNamedGraphMerger;
import org.aksw.jena_sparql_api.rx.io.resultset.SPARQLResultExProcessor;
import org.aksw.jena_sparql_api.rx.io.resultset.SPARQLResultExProcessorBuilder;
import org.aksw.jena_sparql_api.rx.io.resultset.SparqlMappers;
import org.aksw.jena_sparql_api.rx.op.ResultSetMappers;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParser;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.utils.CannedQueryUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.lang.arq.ParseException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableTransformer;
import io.reactivex.rxjava3.core.Maybe;

public class DatasetFlowOps {

    /**
     * GSON instance configured for {@link Dataset} and {@link Node} classes.
     * Used in serialization of datasets
     *
     */
    public static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(Node.class, new TypeAdapterNode())
            .registerTypeHierarchyAdapter(Dataset.class, new TypeAdapterDataset(DatasetFactoryEx::createInsertOrderPreservingDataset))
            .create();

    /**
     * Serialize a key with a given data object on a single line.
     * The provided gson instance needs to have appropriate TypeAdapters registered for the given
     * object.
     *
     * Writes out escape(key)"\t"escape(jsonOf(data))
     *
     * @param key
     * @param dataset
     * @param format
     * @param reosurces
     * @return
     */
    public static String serializeForSort(Gson gson, Node key, Object data) {
        String keyStr = key.isURI() ? key.getURI() : key.getLiteralValue().toString();
        String dataStr = gson.toJson(data);

        String result = StringEscapeUtils.escapeJava(keyStr) + " \t" + StringEscapeUtils.escapeJava(dataStr);
        return result;
    }


    public static <T> T deserializeFromSort(Gson gson, String line, Class<T> clazz) {
        int idx = line.indexOf('\t');
        String encoded = line.substring(idx + 1);
        String decoded = StringEscapeUtils.unescapeJava(encoded);

        T result = gson.fromJson(decoded, clazz);
        return result;
    }

    /**
    *
    * @param cmdSort
    * @param keyQueryParser
    * @param format         Serialization format when passing data to the system
    *                       sort command
    * @return
    */
   public static FlowableTransformer<Dataset, Dataset> createSystemSorter(SysSort cmdSort,
           SparqlQueryParser keyQueryParser) {
       String keyArg = cmdSort.key;

       Function<? super SparqlQueryConnection, Node> keyMapper = ResourceInDatasetFlowOps.createKeyMapper(keyArg,
               keyQueryParser, CannedQueryUtils.DISTINCT_NAMED_GRAPHS);

//			keyQueryParser = keyQueryParser != null
//					? keyQueryParser
//					: SparqlQueryParserWrapperSelectShortForm.wrap(SparqlQueryParserImpl.create(DefaultPrefixes.prefixes));

       // SPARQL : SELECT ?key { ?s eg:hash ?key }
       // Short SPARQL: ?key { ?s eg:hash ?key }
       // LDPath : issue: what to use as the root?

       List<String> sortArgs = SysCalls.createDefaultSortSysCall(cmdSort);

       FlowableTransformer<Dataset, Dataset> sorter = DatasetFlowOps.sysCallSort(keyMapper, sortArgs);

       FlowableTransformer<Dataset, Dataset> result = !cmdSort.merge ? sorter
               : upstream -> upstream.compose(sorter).compose(s -> DatasetFlowOps.mergeConsecutiveDatasets(s));
       return result;
   }


    /**
     * Return a transformer that applies sorting using a system call
     *
     * @param keyMapper
     * @param sysCallArgs
     * @return
     */
    public static FlowableTransformer<Dataset, Dataset> sysCallSort(
            Function<? super SparqlQueryConnection, Node> keyMapper,
            List<String> sysCallArgs) {
        return sysCallSortCore(
                ResultSetMappers.wrapForDataset(keyMapper),
                sysCallArgs,
                (key, data) -> DatasetFlowOps.serializeForSort(GSON, key, data),
                line -> DatasetFlowOps.deserializeFromSort(GSON, line, Dataset.class)
                );
    }



    public static Flowable<Dataset> mergeConsecutiveDatasets(Flowable<Dataset> in) {
        // FIXME This will break if we reuse the flow
        // The merger has to be created on subscription
        ConsecutiveNamedGraphMerger merger = new RDFDataMgrRx.ConsecutiveNamedGraphMerger();

        return
            //in.map(merger::accept)
            in.flatMapMaybe(x -> Maybe.fromCallable(() -> merger.accept(x).orElse(null)))
            .concatWith(Maybe.fromCallable(() -> merger.getPendingDataset().orElse(null)));
    }



    /**
     * Flow making use of line-based sort system call
     *
     * @param <T>
     * @param <K>
     * @param keyMapper
     * @param sysCallArgs
     * @param serializer
     * @param deserializer
     * @return
     */
    public static <T, K> FlowableTransformer<T, T> sysCallSortCore(
            Function<T, K> keyMapper,
            List<String> sysCallArgs,
            BiFunction<K, T, String> serializer,
            Function<String, T> deserializer) {


        return flow -> {
            Flowable<T> r = flow
                .map(ds -> {
                    K key = keyMapper.apply(ds);
                    return Maps.immutableEntry(key, ds);
                })
                .map(e -> serializer.apply(e.getKey(), e.getValue()))
                .compose(FlowableOps.sysCall(sysCallArgs))
                .map(str -> deserializer.apply(str))
                ;
            return r;
        };
    }


    // TODO Set up the processor of type BiConsumer<RDFConnection, SPARQLResultSink>
//    public static BiConsumer<RDFConnection, SPARQLResultSink> createProcessor(
//            //CommandMain cliArgs,
//    		Collection<? extends SparqlStmt> sparqlStmts,
//            PrefixMapping pm,
//            boolean closeSink
//            ) throws FileNotFoundException, IOException, ParseException {
//
//    }


    /**
     * Create a single composite mapper from a 'getter' that extracts a value,
     * a 'mapper' that transforms the value and a 'setter' that returns the final
     * value based on the initial item and the transformed value.
     *
     * @param <I>
     * @param <O>
     * @param <V1>
     * @param <V2>
     * @param valueMapper
     * @param getter
     * @param setter
     * @return
     */
    public static <I, O, V1, V2> Function<I, O> createItemMapper(
            Function<? super V1, ? extends V2> valueMapper,
            Function<? super I, ? extends V1> getter,
            BiFunction<? super I, ? super V2, O> setter) {
        return item -> {
            V1 before = getter.apply(item);
            V2 after = valueMapper.apply(before);
            O r = setter.apply(item, after);
            return r;
        };
    }


    public static <I, O, V1, V2> Function<I, List<O>> createItemMultiMapper(
            Function<? super V1, ? extends Iterable<? extends V2>> valueMapper,
            Function<? super I, ? extends V1> getter,
            BiFunction<? super I, ? super V2, O> setter) {
        return item -> {
            V1 before = getter.apply(item);
            Iterable<? extends V2> after = valueMapper.apply(before);
            List<O> r = Streams.stream(after)
                .map(val -> setter.apply(item, val))
                .collect(Collectors.toList());
            return r;
        };
    }

//    public static <I, O, V1, V2> FlowableTransformer<I, O> mapPropertyValueInParallel(
//    		Function<? super V1, ? extends V2> mapper,
//    		Function<? super I, ? extends V1> getter,
//            BiFunction<? super I, ? super V2, O> setter) {
//
//        return in -> in
//                .zipWith(() -> LongStream.iterate(0, i -> i + 1).iterator(), Maps::immutableEntry)
//                .parallel() //Runtime.getRuntime().availableProcessors(), 8) // Prefetch only few items
//                .runOn(Schedulers.io())
//                //.observeOn(Schedulers.computation())
//                .map(e -> {
//                    I item = e.getKey();
//                    V1 before = getter.apply(item);
//                    V2 after = mapper.apply(before);
//                    O r = setter.apply(item, after);
//                    return Maps.immutableEntry(r, e.getValue());
//                })
//                .sequential()
//                .lift(OperatorLocalOrder.forLong(0l, Entry::getValue))
//                .map(Entry::getKey);
//    }

    /**
     * Crate a mapper that from a set of items can extract Datasets,
     * transform them, and set them back
     *
     * @param <T>
     * @param <X>
     * @param pm
     * @param sparqlStmts
     * @param getDataset
     * @param setDataset
     * @param contextHandler
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ParseException
     */
    public static <T, X> FlowableTransformer<T, X> createMapperDataset(
            //PrefixMapping pm,
            // List<String> sparqlSrcs, //CmdNgsMap cmdMap,
            Collection<? extends SparqlStmt> sparqlStmts,
            Function<? super T, ? extends Dataset> getDataset,
            BiFunction<? super T, ? super Dataset, X> setDataset,
            Supplier<? extends DatasetGraph> datasetGraphSupplier) {

        SPARQLResultExProcessor resultProcessor = SPARQLResultExProcessorBuilder.createForQuadOutput().build();

        Function<RDFConnection, Iterable<Dataset>> connectionBasedMapper = SparqlMappers.createMapperDataset(
                sparqlStmts, resultProcessor, datasetGraphSupplier);
        Function<Dataset, Iterable<Dataset>> datasetBasedMapper = SparqlMappers.mapDatasetToConnection(connectionBasedMapper);

        Function<T, List<X>> itemMapper = createItemMultiMapper(datasetBasedMapper, getDataset, setDataset);

        return RxOps.createParallelFlatMapperOrdered(itemMapper);
    }

//        BiConsumer<RDFConnection, SPARQLResultSink> coreProcessor =
//                DatasetFlowOps.createProcessor(sparqlStmts, pm, true);


        // Wrap the core processor with modifiers for the context
//        BiConsumer<RDFConnection, SPARQLResultSink> processor = (coreConn, sink) -> {
//            RDFConnection c = contextHandler == null
//                ? coreConn
//                : RDFConnectionFactoryEx.wrapWithContext(coreConn, contextHandler);
//
//            coreProcessor.accept(c, sink);
//        };

//
//        Function<Dataset, Dataset> mapper = createMapper(processor);
//
//        return in -> in
//            .zipWith(() -> LongStream.iterate(0, i -> i + 1).iterator(), Maps::immutableEntry)
//            .parallel() //Runtime.getRuntime().availableProcessors(), 8) // Prefetch only few items
//            .runOn(Schedulers.io())
//            //.observeOn(Schedulers.computation())
//            .map(e -> {
//                T item = e.getKey();
//                Dataset before = getDataset.apply(item);
//                Dataset after = mapper.apply(before);
//                X r = setDataset.apply(item, after);
//                return Maps.immutableEntry(r, e.getValue());
//            })
//            // Experiment with performing serialization already in the thread
//            // did not show much benefit
//    //			.map(e -> {
//    //				Dataset tmp = e.getKey();
//    //				String str = toString(tmp, RDFFormat.TRIG_PRETTY);
//    //				return Maps.immutableEntry(str, e.getValue());
//    //			})
//            .sequential()
//            .lift(OperatorLocalOrder.forLong(0l, Entry::getValue))
//            // .lift(OperatorLocalOrder.create(0l, i -> i + 1, (a, b) -> a - b, Entry::getValue))
//            //.sorted((a, b) -> Objects.compare(a.getValue(), b.getValue(), Ordering.natural().re))
//            // .sequential()
////            .doAfterNext(item -> System.err.println("GOT AFTER SEQUENTIAL: " + item.getValue() + " in thread " + Thread.currentThread()))
//    //			.doAfterNext(System.out::println)
////            .doAfterNext(item -> System.err.println("GOT AFTER LOCAL ORDERING: " + item.getValue() + " in thread " + Thread.currentThread()))
//            .map(Entry::getKey);
//    }




    // No longer needed; use SparqlScriptProcessor to get the list of SparqlStmts
    // then use a the SparqlMapBuilder to create the map operation
//    public static FlowableTransformer<Dataset, Dataset> createMapperDataset(Consumer<Context> contextHandler, String ... sparqlResources) {
//        FlowableTransformer<Dataset, Dataset> result;
//        try {
//            result = createMapperDataset(DefaultPrefixes.prefixes, Arrays.asList(sparqlResources), ds -> ds, (before, after) -> after, contextHandler);
//        } catch (IOException | ParseException e) {
//            throw new RuntimeException(e);
//        }
//        return result;
//    }



    // public static final UnaryRelation DISTINCT_NAMED_GRAPHS = Concept.create("GRAPH ?g { ?s ?p ?o }", "g");
    // public static final Query DISTINCT_NAMED_GRAPHS = QueryFactory.create("SELECT DISTINCT ?g { GRAPH ?g { ?s ?p ?o } }");


}
