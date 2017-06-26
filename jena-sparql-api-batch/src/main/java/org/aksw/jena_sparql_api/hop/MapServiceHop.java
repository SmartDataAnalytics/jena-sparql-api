package org.aksw.jena_sparql_api.hop;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.lookup.MapPaginator;
import org.aksw.jena_sparql_api.lookup.MapService;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;

public class MapServiceHop
    implements MapService<Concept, Node, DatasetGraph>
{
    protected QueryExecutionFactory defaultQef;
    protected Hop root;
    protected Concept concept;

    public int chunkSize = 30;

    public MapServiceHop(QueryExecutionFactory defaultQef, Hop root, int chunkSize) {
        super();
        this.defaultQef = defaultQef;
        this.root = root;
        this.chunkSize = chunkSize;
    }


    @Override
    public MapPaginator<Node, DatasetGraph> createPaginator(Concept concept) {
        MapPaginatorHop result = new MapPaginatorHop(defaultQef, root, concept, chunkSize);
        return result;
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
