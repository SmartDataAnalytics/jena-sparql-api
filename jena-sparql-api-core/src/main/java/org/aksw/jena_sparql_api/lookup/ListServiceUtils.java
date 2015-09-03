package org.aksw.jena_sparql_api.lookup;

import java.util.Set;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.mapper.Agg;
import org.aksw.jena_sparql_api.mapper.FunctionResultSetAggregate;
import org.aksw.jena_sparql_api.mapper.MappedConcept;
import org.aksw.jena_sparql_api.utils.ResultSetPart;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.BindingUtils;

public class ListServiceUtils {
    public static <T> ListService<Concept, Node, T> createListServiceAcc(QueryExecutionFactory qef, MappedConcept<T> mappedConcept, boolean isLeftJoin) {

        Concept concept = mappedConcept.getConcept();
        Query query = ConceptUtils.createQueryList(concept);

//        System.out.println(query);
//        if(true) { throw new RuntimeException("foo"); }


        Agg<T> agg = mappedConcept.getAggregator();

        //Var  rowId = Var.alloc("rowId");

        // TODO Set up a projection using the grouping variable and the variables referenced by the aggregator
        Set<Var> vars = agg.getDeclaredVars();
        for(Var var : vars) {
            query.getProject().add(var);
        }
        //query.setQueryResultStar(true);

        ListServiceSparqlQuery ls = new ListServiceSparqlQuery(qef, query, concept.getVar(), isLeftJoin);
        FunctionResultSetAggregate<T> fn = new FunctionResultSetAggregate<T>(agg);
        ListServiceTransformItem<Concept, Node, ResultSetPart, T> result = ListServiceTransformItem.create(ls, fn);

        return result;
    }

    public static <T> ListService<Concept, Node, T> createListServiceMappedConcept(QueryExecutionFactory qef, MappedConcept<T> mappedConcept, boolean isLeftJoin) {
        ListService<Concept, Node, T> result = createListServiceAcc(qef, mappedConcept, isLeftJoin);

        // Add a transformer that actually retrieves the value from the acc structure
//        ListService<Concept, Node, T> result = new ListServiceTransformItem(ls, function(accEntries) {
//            var r = accEntries.map(function(accEntry) {
//                var s = accEntry.val.getValue();
//                return s;
//            });
//
//            return r;
//        });

        return result;
    }
}
