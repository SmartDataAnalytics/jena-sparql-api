package org.aksw.jena_sparql_api.lookup;

import java.util.Map;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.apache.jena.graph.Node;


public class ListServiceConcept
    implements ListService<Concept, Node, Node>
{
    protected QueryExecutionFactory qef;

    public ListServiceConcept(QueryExecutionFactory qef) {
        this.qef = qef;
    }


    @Override
    public Paginator<Node, Node> createPaginator(Concept concept) {
        PaginatorConcept result = new PaginatorConcept(qef, concept);
        return result;
    }


    public static void main(String[] args) {
        QueryExecutionFactory qef = new QueryExecutionFactoryHttp("http://dbpedia.org/sparql");
        ListService<Concept, Node, Node> ls = new ListServiceConcept(qef);

        Concept concept = ConceptUtils.listAllPredicates;

        CountInfo countInfo;

        countInfo = ls.fetchCount(concept, 2l, null);
        System.out.println(countInfo);

        countInfo = ls.fetchCount(concept, 3l, null);
        System.out.println(countInfo);

        countInfo = ls.fetchCount(concept, 4l, null);
        System.out.println(countInfo);

        countInfo = ls.fetchCount(concept, null, null);
        System.out.println(countInfo);


        Map<Node, Node> data = ls.fetchData(concept, null, null);

        System.out.println(data);
    }
}
