package org.aksw.jena_sparql_api.lookup;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.QueryExecutionUtils;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.aggregate.AggCount;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementSubQuery;

import com.google.common.collect.Range;

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

    @Override
    public Map<Node, Node> fetchMap(Range<Long> range) {
        Query query = concept.asQuery();
        QueryUtils.applyRange(query, range);
//        query.setLimit(limit == null ? Query.NOLIMIT : limit);
//        query.setOffset(offset == null ? Query.NOLIMIT : offset);

        List<Node> tmp = QueryExecutionUtils.executeList(qef, query, concept.getVar());

        //List<Entry<Node, Node>> result = new ArrayList<Entry<Node, Node>>(tmp.size());
        Map<Node, Node> result = new LinkedHashMap<Node, Node>();
        for(Node node : tmp) {
            //Entry<Node, Node> item = Pair.create(node, node);
            result.put(node, node);
        }


        return result;
    }

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
    public CountInfo fetchCount(Long itemLimit, Long rowLimit) {
        Var c = Var.alloc("_c_");
        Long limit = itemLimit == null ? null : itemLimit + 1;
        Query query = createQueryCount(concept, limit, rowLimit, c);

        //if(true) { return null; }

        Node countNode = QueryExecutionUtils.executeSingle(qef, query, c);
        long count = ((Number)countNode.getLiteralValue()).longValue();

        boolean hasMoreItems = false;

        if(itemLimit != null && count > itemLimit) {
            count = itemLimit;
            hasMoreItems = true;
        }


        CountInfo result = new CountInfo(count, hasMoreItems, itemLimit);

        return result;
    }

    @Override
    public Stream<Entry<Node, Node>> apply(Range<Long> range) {
        Map<Node, Node> map = fetchMap(range);
        return map.entrySet().stream();
    }

}
