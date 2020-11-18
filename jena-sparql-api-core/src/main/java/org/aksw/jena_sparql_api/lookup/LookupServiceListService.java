package org.aksw.jena_sparql_api.lookup;

import java.util.Map.Entry;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.apache.jena.graph.Node;

import io.reactivex.rxjava3.core.Flowable;

public class LookupServiceListService<V>
    implements LookupService<Node, V>
{
    private MapService<Concept, Node, V> listService;

    public LookupServiceListService(MapService<Concept, Node, V> listService) {
        super();
        this.listService = listService;
    }

    public static boolean jena_jira_bug_1484_message_displayed = false;

    @Override
    public Flowable<Entry<Node, V>> apply(Iterable<Node> nodes) {
//        ExprList args = new ExprList();
//        for(Node node : nodes) {
//            Expr expr = NodeValue.makeNode(node);
//            args.add(expr);
//        }

//        Expr e = new E_OneOf(new ExprVar(Vars.s), args);
//        Element filter = new ElementFilter(e);
//        Concept concept = new Concept(filter, Vars.s);


        // VALUES vs FILTER has dire consequences when using GraphSparqlService:
        // Jena will prioritize a VALUES clause, over triple patterns, however
        // if a filter is given, triple patterns are evaluated first

        if(!jena_jira_bug_1484_message_displayed) {
            System.err.println("JENA BUG 1484: FIXME - Once the bug is fixed - switch implementation to use VALUE instead of FILTER");
            jena_jira_bug_1484_message_displayed = true;
        }

         Concept concept = ConceptUtils.createConcept(nodes);
        //Concept concept = ConceptUtils.createFilterConcept(nodes);

        // TODO update once list service also uses futures or flows
        //result = CompletableFuture.completedFuture(listService.fetchData(concept, null, null));
        Flowable<Entry<Node, V>> result = listService.createPaginator(concept).apply(null);
        return result;
        //return result;
    }


    public static <V> LookupServiceListService<V> create(MapService<Concept, Node, V> listService) {
        LookupServiceListService<V> result = new LookupServiceListService<>(listService);
        return result;
    }
}
