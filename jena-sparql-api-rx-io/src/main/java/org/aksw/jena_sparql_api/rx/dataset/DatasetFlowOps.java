package org.aksw.jena_sparql_api.rx.dataset;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.aksw.jena_sparql_api.io.json.TypeAdapterDataset;
import org.aksw.jena_sparql_api.io.json.TypeAdapterNode;
import org.aksw.jena_sparql_api.rx.DatasetFactoryEx;
import org.aksw.jena_sparql_api.rx.RDFDataMgrRx;
import org.aksw.jena_sparql_api.rx.RDFDataMgrRx.ConsecutiveNamedGraphMerger;
import org.aksw.jena_sparql_api.rx.op.ResultSetMappers;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

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


}
