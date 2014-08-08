package org.aksw.jena_sparql_api.lookup;

import java.util.List;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.QueryExecutionUtils;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.ExprAggregator;
import com.hp.hpl.jena.sparql.expr.aggregate.AggCount;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery;


public class ListServiceConcept
    implements ListService<Concept, Node>
{
    private QueryExecutionFactory qef;
    
    public ListServiceConcept(QueryExecutionFactory qef) {
        this.qef = qef;
    }
    
    @Override
    public List<Node> fetchData(Concept concept, Long limit, Long offset) {
        Query query = concept.asQuery();
        query.setLimit(limit == null ? Query.NOLIMIT : limit);
        query.setOffset(offset == null ? Query.NOLIMIT : offset);
        
        List<Node> result = QueryExecutionUtils.executeList(qef, query, concept.getVar());
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
    public CountInfo fetchCount(Concept concept, Long itemLimit) {
        Var c = Var.alloc("_c_");
        Long limit = itemLimit == null ? null : itemLimit + 1;
        Query query = createQueryCount(concept, limit, null, c);

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
    
    public static void main(String[] args) {
        QueryExecutionFactory qef = new QueryExecutionFactoryHttp("http://dbpedia.org/sparql");
        ListService<Concept, Node> ls = new ListServiceConcept(qef);
        
        Concept concept = ConceptUtils.listAllPredicates;
        
        CountInfo countInfo;

        countInfo = ls.fetchCount(concept, 2l);
        System.out.println(countInfo);

        countInfo = ls.fetchCount(concept, 3l);
        System.out.println(countInfo);

        countInfo = ls.fetchCount(concept, 4l);
        System.out.println(countInfo);

        countInfo = ls.fetchCount(concept, null);
        System.out.println(countInfo);

        
        List<Node> data = ls.fetchData(concept, null, null);
        
        System.out.println(data);
    }
}
