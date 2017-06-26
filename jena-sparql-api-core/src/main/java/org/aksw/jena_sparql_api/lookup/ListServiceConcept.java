package org.aksw.jena_sparql_api.lookup;

import java.util.Map;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.apache.jena.graph.Node;


public class ListServiceConcept
    implements MapService<Concept, Node, Node>
{
    protected QueryExecutionFactory qef;

    public ListServiceConcept(QueryExecutionFactory qef) {
        this.qef = qef;
    }


    @Override
    public MapPaginator<Node, Node> createPaginator(Concept concept) {
        MapPaginatorConcept result = new MapPaginatorConcept(qef, concept);
        return result;
    }


    public static void main(String[] args) {
        QueryExecutionFactory qef = new QueryExecutionFactoryHttp("http://dbpedia.org/sparql");
        MapService<Concept, Node, Node> ls = new ListServiceConcept(qef);

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
