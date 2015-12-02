package org.aksw.jena_sparql_api.hop;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.jena_sparql_api.batch.cli.main.MainBatchWorkflow;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.aksw.jena_sparql_api.lookup.CountInfo;
import org.aksw.jena_sparql_api.lookup.ListService;
import org.aksw.jena_sparql_api.lookup.ListServiceUtils;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.lookup.LookupServiceListService;
import org.aksw.jena_sparql_api.lookup.LookupServicePartition;
import org.aksw.jena_sparql_api.lookup.LookupServiceUtils;
import org.aksw.jena_sparql_api.mapper.MappedQuery;
import org.aksw.jena_sparql_api.utils.DatasetGraphUtils;

import com.google.common.base.Functions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.DatasetGraph;

public class ListServiceHop
    implements ListService<Concept, Node, DatasetGraph>
{
    protected QueryExecutionFactory defaultQef;
    protected Hop root;

    public static int chunkSize = 30;

    public ListServiceHop(QueryExecutionFactory defaultQef, Hop root) {
        super();
        this.defaultQef = defaultQef;
        this.root = root;
    }

    @Override
    public Map<Node, DatasetGraph> fetchData(Concept concept, Long limit, Long offset) {
        List<Node> sourceNodes = ServiceUtils.fetchList(defaultQef, concept, limit, offset);

        Map<Node, DatasetGraph> result = new HashMap<Node, DatasetGraph>();
        //HashMultimap.<Node, Node>create()
        execRec(root, sourceNodes, result, defaultQef, null);

        //MainBatchWorkflow.write(System.out, result);

        return result;
    }


    @Override
    public CountInfo fetchCount(Concept concept, Long itemLimit, Long rowLimit) {
        CountInfo result = ServiceUtils.fetchCountConcept(defaultQef, concept, itemLimit, rowLimit);
        return result;
    }



    public static void execQueriesHop(QueryExecutionFactory qef, Collection<Node> nodes, Collection<MappedQuery<DatasetGraph>> mappedQueries, Map<Node, DatasetGraph> result) {
        for(MappedQuery<DatasetGraph> mappedQuery : mappedQueries) {
            ListService<Concept, Node, DatasetGraph> listService = ListServiceUtils.createListServiceMappedQuery(qef, mappedQuery, true);
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

        Relation relation = hopRelation.getRelation();
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
}


//List<MappedQuery<DatasetGraph>> mappedQueries = hop.getQueries();
//execQueriesHop(qef, nodes, mappedQueries, result);
//
//Multimap<Relation, Hop> relationToData = hop.getRelationToData();
//Map<Relation, Collection<Hop>> map = relationToData.asMap();
//
//for(Entry<Relation, Collection<Hop>> entry : map.entrySet()) {
//  Relation relation = entry.getKey();
//  Var sourceVar = relation.getSourceVar();
//
//  AggList<Node> agg = AggList.create(AggLiteral.create(BindingMapperProjectVar.create(relation.getTargetVar())));
//  Query query = RelationUtils.createQuery(relation);
//  MappedQuery<List<Node>> mappedQuery = MappedQuery.create(query, sourceVar, agg);
//  LookupService<Node, List<Node>> lookupService = LookupServiceUtils.createLookupService(qef, mappedQuery);
//
//  Map<Node, List<Node>> tmpRelatedNodes = lookupService.apply(nodes);
//  Set<Node> relatedNodes = Sets.newHashSet(
//          FluentIterable.from(tmpRelatedNodes.values()).transformAndConcat(Functions.<List<Node>>identity())); // TODO move to a flatMap util function
//
//  Collection<Hop> subHops = entry.getValue();
//  //relation.getE
//
//  //List<Hop> subHops = //hopItem.getHops();
//  for(Hop subHop : subHops) {
//      //QueryExecutionFactory subQef = subHop.getQef();
//      execRec(subHop, relatedNodes, null, null, result, qef);
//  }
//}
//
