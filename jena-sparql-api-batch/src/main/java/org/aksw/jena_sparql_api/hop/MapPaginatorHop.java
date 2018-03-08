package org.aksw.jena_sparql_api.hop;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.aksw.jena_sparql_api.lookup.CountInfo;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.lookup.LookupServiceListService;
import org.aksw.jena_sparql_api.lookup.LookupServicePartition;
import org.aksw.jena_sparql_api.lookup.LookupServiceUtils;
import org.aksw.jena_sparql_api.lookup.MapPaginator;
import org.aksw.jena_sparql_api.lookup.MapService;
import org.aksw.jena_sparql_api.lookup.MapServiceUtils;
import org.aksw.jena_sparql_api.mapper.MappedQuery;
import org.aksw.jena_sparql_api.utils.DatasetGraphUtils;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;

import com.google.common.base.Functions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;

public class MapPaginatorHop
    implements MapPaginator<Node, DatasetGraph>
{
    protected QueryExecutionFactory defaultQef;
    protected Hop root;
    protected Concept concept;

    public static int chunkSize = 30;

    public MapPaginatorHop(QueryExecutionFactory defaultQef, Hop root, Concept concept, int chunkSize) {
        super();
        this.defaultQef = defaultQef;
        this.root = root;
        this.concept = concept;

        // TODO FIXME The chunkSize parameter needs to be handed over to static functions...
        //this.chunkSize = chunkSize;
    }

    @Override
    public Map<Node, DatasetGraph> fetchMap(Range<Long> range) {
        Long limit = QueryUtils.rangeToLimit(range);
        Long offset = QueryUtils.rangeToOffset(range);

        List<Node> sourceNodes = ServiceUtils.fetchList(defaultQef, concept, limit, offset);

        Map<Node, DatasetGraph> result = new HashMap<Node, DatasetGraph>();
        //HashMultimap.<Node, Node>create()
        execRec(root, sourceNodes, result, defaultQef, null);

        //MainBatchWorkflow.write(System.out, result);

        return result;
    }


    @Override
    public CountInfo fetchCount(Long itemLimit, Long rowLimit) {
        CountInfo result = ServiceUtils.fetchCountConcept(defaultQef, concept, itemLimit, rowLimit);
        return result;
    }



    public static void execQueriesHop(QueryExecutionFactory qef, Collection<Node> nodes, Collection<MappedQuery<DatasetGraph>> mappedQueries, Map<Node, DatasetGraph> result) {
        for(MappedQuery<DatasetGraph> mappedQuery : mappedQueries) {
            MapService<Concept, Node, DatasetGraph> listService = MapServiceUtils.createListServiceMappedQuery(qef, mappedQuery, true);
            LookupService<Node, DatasetGraph> lookupService = LookupServiceListService.create(listService);

            lookupService = LookupServicePartition.create(lookupService, chunkSize);


            Map<Node,DatasetGraph> nodeToGraph = lookupService.apply(nodes);
            DatasetGraphUtils.mergeInPlace(result, nodeToGraph);
        }
    }

    public static void processHopQuery(HopQuery hopQuery, Collection<Node> sourceNodes, Map<Node, DatasetGraph> result, QueryExecutionFactory defaultQef, Multimap<Node, Node> back) {
        QueryExecutionFactory qef = hopQuery.getQef();
        qef = (qef == null ? defaultQef : qef);

        MappedQuery<DatasetGraph> mappedQuery = hopQuery.getMappedQuery();
        LookupService<Node, DatasetGraph> ls = LookupServiceUtils.createLookupService(qef, mappedQuery);

        ls = LookupServicePartition.create(ls, chunkSize);

        Map<Node, DatasetGraph> tmpMap = ls.apply(sourceNodes);


        Map<Node, DatasetGraph> map;
        if(back != null) {
            map = new HashMap<Node, DatasetGraph>();
            for(Entry<Node, DatasetGraph> entry : tmpMap.entrySet()) {
                Node tmpNode = entry.getKey();
                DatasetGraph datasetGraph = entry.getValue();
                Collection<Node> keys = back.get(tmpNode);

                for(Node key : keys) {
                    map.put(key, datasetGraph);
                }

            }
        } else {
            map = tmpMap;
        }


        DatasetGraphUtils.mergeInPlace(result, map);
    }

    public static void processHopQueries(List<HopQuery> hopQueries, Collection<Node> sourceNodes, Map<Node, DatasetGraph> result, QueryExecutionFactory defaultQef, Multimap<Node, Node> back) {
        for(HopQuery hopQuery : hopQueries) {
            processHopQuery(hopQuery, sourceNodes, result, defaultQef, back);
        }
    }

    public static <T, C extends Iterable<T>> Iterable<T> flatMap(Map<T, C> map) {
        Iterable<T> result = FluentIterable.from(map.values()).transformAndConcat(Functions.<C>identity());
        return result;
    }

    public static void processHopRelations(List<HopRelation> hopRelations, Collection<Node> sourceNodes, Map<Node, DatasetGraph> result, QueryExecutionFactory defaultQef, Multimap<Node, Node> back) {
        for(HopRelation hopRelation : hopRelations) {
            processHopRelation(hopRelation, sourceNodes, result, defaultQef, back);
        }
    }

    public static void processHopRelation(HopRelation hopRelation, Collection<Node> sourceNodes, Map<Node, DatasetGraph> result, QueryExecutionFactory defaultQef, Multimap<Node, Node> back) {
        QueryExecutionFactory qef = hopRelation.getQef();
        qef = (qef == null ? defaultQef : qef);

        BinaryRelation relation = hopRelation.getRelation();
        LookupService<Node, List<Node>> ls = LookupServiceUtils.createLookupService(qef, relation);
        ls = LookupServicePartition.create(ls, chunkSize);


        Map<Node, List<Node>> map = ls.apply(sourceNodes);

        Multimap<Node, Node> tmpMm = HashMultimap.create();
        for (Entry<Node, List<Node>> entry : map.entrySet()) {
            tmpMm.putAll(entry.getKey(), entry.getValue());
        }

        Multimap<Node, Node> tmpBack = Multimaps.invertFrom(tmpMm, HashMultimap.<Node, Node>create());


        Multimap<Node, Node> nextBack;
        if(back != null) {
            nextBack = HashMultimap.<Node, Node>create();
            for(Entry<Node, Node> entry : nextBack.entries()) {
                Node src = entry.getKey();
                Node tmp = entry.getValue();

                Collection<Node> tgts = back.get(tmp);

                for(Node tgt : tgts) {
                    nextBack.put(src, tgt);
                }
            }
        } else {
            nextBack = tmpBack;
        }

        //Multimaps.invertFrom(Multimaps.<Node, Node>forMap(map), HashMultimap.<Node, Node>create());



        Set<Node> relatedNodes = Sets.<Node>newHashSet(flatMap(map));

        for(Hop hop : hopRelation.getHops()) {
            execRec(hop, relatedNodes, result, qef, nextBack);
        }
    }

    public static void execRec(Hop hop,  Collection<Node> sourceNodes,  Map<Node, DatasetGraph> result, QueryExecutionFactory defaultQef, Multimap<Node, Node> back) {

        List<HopQuery> hopQueries = hop.getHopQueries();
        List<HopRelation> hopRelations = hop.getHopRelations();

        processHopQueries(hopQueries, sourceNodes, result, defaultQef, back);
        processHopRelations(hopRelations, sourceNodes, result, defaultQef, back);
    }

    @Override
    public Stream<Entry<Node, DatasetGraph>> apply(Range<Long> range) {
        return fetchMap(range).entrySet().stream();
    }

}
