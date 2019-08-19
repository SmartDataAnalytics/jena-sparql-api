package org.aksw.jena_sparql_api.lookup;

import java.util.Map.Entry;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.aggregate.AggCount;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementSubQuery;

import com.google.common.collect.Maps;
import com.google.common.collect.Range;

import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * TODO Convert to a ListService
 *
 * @author raven
 *
 */
public class MapPaginatorConcept
    implements MapPaginator<Node, Node>
{
    protected QueryExecutionFactory qef;
    protected Concept concept;

    public MapPaginatorConcept(QueryExecutionFactory qef, Concept concept) {
        this.qef = qef;
        this.concept = concept;
    }

//    @Override
//    public Map<Node, Node> fetchMap(Range<Long> range) {
//        Query query = concept.asQuery();
//        QueryUtils.applyRange(query, range);
////        query.setLimit(limit == null ? Query.NOLIMIT : limit);
////        query.setOffset(offset == null ? Query.NOLIMIT : offset);
//
//        List<Node> tmp = QueryExecutionUtils.executeList(qef, query, concept.getVar());
//
//        //List<Entry<Node, Node>> result = new ArrayList<Entry<Node, Node>>(tmp.size());
//        Map<Node, Node> result = new LinkedHashMap<Node, Node>();
//        for(Node node : tmp) {
//            //Entry<Node, Node> item = Pair.create(node, node);
//            result.put(node, node);
//        }
//
//
//        return result;
//    }

    public static Query createSubQuery(Query query, Var var) {
        Element esq = new ElementSubQuery(query);

        Query result = new Query();
        result.setQuerySelectType();
        result.getProject().add(var);
        result.setQueryPattern(esq);

        return result;
    }

    public static Query createQueryCount(Concept concept, Long itemLimit, Long rowLimit, Var resultVar) {
        Query subQuery = concept.asQuery();

        if(rowLimit != null) {
            subQuery.setDistinct(false);
            subQuery.setLimit(rowLimit);

            subQuery = createSubQuery(subQuery, concept.getVar());
            subQuery.setDistinct(true);
        }

        if(itemLimit != null) {
            subQuery.setLimit(itemLimit);
        }

        Element esq = new ElementSubQuery(subQuery);

        Query result = new Query();
        result.setQuerySelectType();
        result.getProject().add(resultVar, new ExprAggregator(concept.getVar(), new AggCount()));
        result.setQueryPattern(esq);

        return result;
    }

    /**
     *
     * @param itemLimit number of distinct resources to scan before returning a count early
     */
    @Override
    public Single<Range<Long>> fetchCount(Long itemLimit, Long rowLimit) {
        Var c = Var.alloc("_c_");
        Long limit = itemLimit == null ? null : itemLimit + 1;
        Query query = createQueryCount(concept, limit, rowLimit, c);

        //if(true) { return null; }

        Single<Range<Long>> result = SparqlRx.execSelectRaw(() -> qef.createQueryExecution(query))
        	.map(b -> b.get(c))
        	.map(countNode -> ((Number)countNode.getLiteralValue()).longValue())
        	.map(count -> {
        		boolean hasMoreItems = false;
                if(itemLimit != null && count > itemLimit) {
                    count = itemLimit;
                    hasMoreItems = true;
                }

                Range<Long> r = hasMoreItems ? Range.atLeast(itemLimit) : Range.singleton(count);        		
                return r;
        	})
        	.single(null);
        
        return result;
    }

    @Override
    public Flowable<Entry<Node, Node>> apply(Range<Long> range) {
    	Query query = concept.asQuery();
    	QueryUtils.applyRange(query, range);

    	//Query query = createQueryCount(concept, itemLimit, rowLimit, resultVar)
    	return SparqlRx.execSelectRaw(() -> qef.createQueryExecution(query))
    			.map(b -> b.get(b.vars().next()))
    			.map(node -> Maps.immutableEntry(node, node));
    	
//    	apply(range);
//        Map<Node, Node> map = fetchMap(range);
//        return map.entrySet().stream();
    }

}
