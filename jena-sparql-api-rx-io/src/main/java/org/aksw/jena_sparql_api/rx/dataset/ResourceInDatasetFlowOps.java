package org.aksw.jena_sparql_api.rx.dataset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.commons.io.syscall.SysCalls;
import org.aksw.commons.io.syscall.sort.SysSort;
import org.aksw.commons.rx.op.FlowableOperatorSequentialGroupBy;
import org.aksw.jena_sparql_api.rdf.model.ext.dataset.api.NodesInDataset;
import org.aksw.jena_sparql_api.rdf.model.ext.dataset.api.ResourceInDataset;
import org.aksw.jena_sparql_api.rdf.model.ext.dataset.impl.GraphNameAndNode;
import org.aksw.jena_sparql_api.rdf.model.ext.dataset.impl.NodesInDatasetImpl;
import org.aksw.jena_sparql_api.rdf.model.ext.dataset.impl.ResourceInDatasetImpl;
import org.aksw.jena_sparql_api.rx.RDFDataMgrRx.ConsecutiveNamedGraphMergerCore;
import org.aksw.jena_sparql_api.rx.op.ResultSetMappers;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParser;
import org.aksw.jena_sparql_api.utils.CannedQueryUtils;
import org.aksw.jena_sparql_api.utils.DatasetUtils;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.apache.jena.ext.com.google.common.base.Strings;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

// import com.github.davidmoten.rx2.flowable.Transformers;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableTransformer;
import io.reactivex.rxjava3.core.Maybe;



class ConsecutiveGraphMergerMergerForResourceInDataset
    extends ConsecutiveNamedGraphMergerCore<NodesInDatasetImpl>
{
    protected Map<Node, List<Node>> graphToNodes= new HashMap<>();

    public Optional<NodesInDatasetImpl> accept(NodesInDataset grid) {
        Dataset dataset = grid.getDataset();

        for(GraphNameAndNode e : grid.getGraphNameAndNodes()) {
            Node g = NodeFactory.createURI(e.getGraphName());

            // Ensure that the referenced graphs are actually mentioned in the datasets,
            // otherwise they will fill up the memory but will never be returned
            boolean isNodeInGraph = DatasetUtils.containsDefaultOrNamedModel(dataset, g);

            if(isNodeInGraph) {
                graphToNodes
                    .computeIfAbsent(g, x -> new ArrayList<>())
                    .add(e.getNode());
            }
        }

        Optional<NodesInDatasetImpl> result = super.accept(dataset);
        return result;
    }

    @Override
    protected NodesInDataset mapResult(Set<Node> readyGraphs, Dataset dataset) {
        Set<GraphNameAndNode> gans = readyGraphs.stream()
            .flatMap(g -> graphToNodes.getOrDefault(g, Collections.emptyList())
                    .stream().map(n -> new GraphNameAndNode(g.getURI(), n)))
            .collect(Collectors.toSet());

        for(Node node : readyGraphs) {
            graphToNodes.remove(node);
        }

        NodesInDataset result = new NodesInDatasetImpl(dataset, gans);

        return result;
    }
}


/**
 * This class provides operators to map between individual objects and flowables of
 * Datasets, ResourceInDataset and GroupedResourceInDataset types.
 *
 * Dataset is a set of quads.
 *
 * ResourceInDataset is an abstraction providing Jena's convenient Resource API to a specific
 * resource in one specific model of the dataset's ones.
 * A change to that resource does not affect resources with the
 * same blank node / uri label in another model.
 *
 * GroupedResourceInDataset is used for efficiency in operators as it prevents
 * duplicate processing of datasets from which many ResourceInDataset instances are created.
 *
 *
 * @author raven
 *
 */
public class ResourceInDatasetFlowOps {


    /**
     * Return a flow over the natural resources in the named graphs of a dataset.
     * A natural resource is a node that has has the same name (IRI) as the graph.
     *
     * @param dataset
     * @return
     */
    public static Flowable<ResourceInDataset> naturalResources(Dataset dataset) {
        return Flowable.fromIterable(() -> dataset.listNames())
            .map(graphName -> {
                Node node = NodeFactory.createURI(graphName);
                return new ResourceInDatasetImpl(dataset, graphName, node);
            });
    }

    /**
     * Operator that marks (graph, node) pairs from a dataset using a
     * SELECT query with two result variables and returns them in a
     * GroupedResourceInDataset object.
     *
     *
     * @param nodeSelector
     * @return
     */
    public static Function<Dataset, NodesInDatasetImpl> mapToGroupedResourceInDataset(Query nodeSelector) {

        // TODO Ensure that the query result has two columns
        Function<? super SparqlQueryConnection, Collection<List<Node>>> mapper = ResultSetMappers.createTupleMapper(nodeSelector);

        return dataset -> {
            try(SparqlQueryConnection conn = RDFConnectionFactory.connect(dataset)) {
                Collection<List<Node>> tuples = mapper.apply(conn);

                Set<GraphNameAndNode> gan = tuples.stream()
                    .map(tuple -> {
                        Node g = tuple.get(0);
                        Node node = tuple.get(1);

                        String graphName = g.getURI();

                        return new GraphNameAndNode(graphName, node);
                    })
                    .collect(Collectors.toSet());

                NodesInDatasetImpl r = new NodesInDatasetImpl(dataset, gan);
                return r;
            }
        };
    }


    /**
     * Accumulate consecutive ResourceInDataset items which share the same
     * Dataset or underlying DatasetGraph by reference equality into an
     * Entry<Dataset, List<Node>>

     * @return
     */
    public static FlowableTransformer<ResourceInDataset, NodesInDatasetImpl> groupedResourceInDataset() {
        return upstream -> upstream
                .lift(FlowableOperatorSequentialGroupBy.<ResourceInDataset, Dataset, List<ResourceInDataset>>create(
                        ResourceInDataset::getDataset,
                        (k1, k2) -> k1 == k2 || k1.asDatasetGraph() == k2.asDatasetGraph(),
                        key -> new ArrayList<>(),
                        (list, item) -> list.add(item)))
                .map(Entry::getValue)
//                .compose(Transformers.<ResourceInDataset>toListWhile(
//                        (list, t) -> {
//                            boolean r = list.isEmpty();
//                            if(!r) {
//                                ResourceInDataset proto = list.get(0);
//                                r = proto.getDataset() == t.getDataset() ||
//                                    proto.getDataset().asDatasetGraph() == t.getDataset().asDatasetGraph();
//                            }
//                            return r;
//                        }))
                .map(list -> {
                    ResourceInDataset proto = list.get(0);
                    Dataset ds = proto.getDataset();
                    Set<GraphNameAndNode> nodes = list.stream()
                            .map(r -> new GraphNameAndNode(r.getGraphName(), r.asNode()))
                            .collect(Collectors.toSet());

                    return new NodesInDatasetImpl(ds, nodes);
                });
    }

    public static List<ResourceInDataset> ungroupResourceInDataset(NodesInDataset grid) {
        List<ResourceInDataset> result = grid.getGraphNameAndNodes().stream()
                .map(gan -> new ResourceInDatasetImpl(grid.getDataset(), gan.getGraphName(), gan.getNode()))
                .collect(Collectors.toList());

        return result;
    }

    /**
     * Intended to be used with flatMap
     *
     * flow.flatMap(...)
     *
     * @param grid
     * @return
     */
    public static Flowable<ResourceInDataset> ungrouperResourceInDataset(NodesInDataset grid) {
        return Flowable.fromIterable(ungroupResourceInDataset(grid));
    }


    public static FlowableTransformer<NodesInDatasetImpl, NodesInDatasetImpl> sysCallSort(
            Function<? super SparqlQueryConnection, Node> keyMapper,
            List<String> sysCallArgs) {

        return DatasetFlowOps.sysCallSortCore(
                gid -> ResultSetMappers.wrapForDataset(keyMapper).apply(gid.getDataset()),
                sysCallArgs,
                (key, data) -> DatasetFlowOps.serializeForSort(DatasetFlowOps.GSON, key, data),
                line -> DatasetFlowOps.deserializeFromSort(DatasetFlowOps.GSON, line, NodesInDatasetImpl.class)
                );
    }


    public static Function<? super SparqlQueryConnection, Node> createKeyMapper(
            String keyArg,
            Function<? super String, ? extends Query> queryParser,
            Query fallback) {
        //Function<Dataset, Node> keyMapper;

        Query effectiveKeyQuery;
        boolean useFallback = Strings.isNullOrEmpty(keyArg);
        if(!useFallback) {
            effectiveKeyQuery = queryParser.apply(keyArg);
            QueryUtils.optimizePrefixes(effectiveKeyQuery);
        } else {
            effectiveKeyQuery = fallback;
        }

        Function<? super SparqlQueryConnection, Node> result = ResultSetMappers.createNodeMapper(effectiveKeyQuery, NodeFactory.createLiteral(""));
        return result;
    }


    public static FlowableTransformer<NodesInDatasetImpl, NodesInDatasetImpl> createSystemSorter(
            SysSort cmdSort,
            SparqlQueryParser keyQueryParser) {
        String keyArg = cmdSort.key;

        Function<? super SparqlQueryConnection, Node> keyMapper = createKeyMapper(keyArg, keyQueryParser, CannedQueryUtils.DISTINCT_NAMED_GRAPHS);


//		keyQueryParser = keyQueryParser != null
//				? keyQueryParser
//				: SparqlQueryParserWrapperSelectShortForm.wrap(SparqlQueryParserImpl.create(DefaultPrefixes.prefixes));

        // SPARQL      : SELECT ?key { ?s eg:hash ?key }
        // Short SPARQL: ?key { ?s eg:hash ?key }
        // LDPath      : issue: what to use as the root?


        List<String> sortArgs = SysCalls.createDefaultSortSysCall(cmdSort);

        return sysCallSort(keyMapper, sortArgs);
    }

    public static Flowable<NodesInDatasetImpl> mergeConsecutiveResourceInDatasets(Flowable<NodesInDatasetImpl> in) {
        // FIXME This will break if we reuse the flow
        // The merger has to be created on subscription
        ConsecutiveGraphMergerMergerForResourceInDataset merger = new ConsecutiveGraphMergerMergerForResourceInDataset();

        return
            in.flatMapMaybe(x -> Maybe.fromCallable(() -> merger.accept(x).orElse(null)))
            .concatWith(Maybe.fromCallable(() -> merger.getPendingDataset().orElse(null)));
    }


//	public static FlowableTransformer<ResourceInDataset, ResourceInDataset> createSystemSorter2(
//			Function<? super SparqlQueryConnection, Node> keyMapper,
//			List<String> sysCallArgs,
//			boolean mergeConsecutiveDatasets) {
//
////		List<String> sortArgs = SysCalls.createDefaultSortSysCall(cmdSort);
//
//
//		return upstream ->
//			upstream
//				.compose(ResourceInDatasetFlowOps.groupedResourceInDataset())
//				.map(group -> {
//					Dataset ds = group.getDataset();
//					Node key;
//					try(RDFConnection conn = RDFConnectionFactory.connect(ds)) {
//						key = keyMapper.apply(conn);
//					}
//					return Maps.immutableEntry(key, group);
//				})
//				.map(e -> DatasetFlowOps.serializeForSort(DatasetFlowOps.gson, e.getKey(), e.getValue()))
//				.compose(FlowableOps.sysCall(sysCallArgs))
//				.map(line -> DatasetFlowOps.deserializeFromSort(DatasetFlowOps.gson, line, GroupedResourceInDataset.class))
//				.flatMap(ResourceInDatasetFlowOps::ungrouperResourceInDataset)
//			;
//	}


    /**
     * Adapter to create a transformed for {@link ResourceInDataset} based on one for {@link NodesInDatasetImpl}.
     *
     * @param innerTransform
     * @return
     */
    public static FlowableTransformer<ResourceInDataset, ResourceInDataset> createTransformerFromGroupedTransform(FlowableTransformer<NodesInDatasetImpl, NodesInDatasetImpl> innerTransform) {
        return upstream -> upstream
            .compose(ResourceInDatasetFlowOps.groupedResourceInDataset())
            .compose(innerTransform)
            .flatMap(ResourceInDatasetFlowOps::ungrouperResourceInDataset);
    }

}
