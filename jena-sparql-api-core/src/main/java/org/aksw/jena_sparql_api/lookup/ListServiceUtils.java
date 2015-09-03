package org.aksw.jena_sparql_api.lookup;

import java.util.Set;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.mapper.Agg;
import org.aksw.jena_sparql_api.mapper.MappedConcept;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.BindingUtils;

public class ListServiceUtils {
    public static <T> ListService<Concept, Node, T> createListServiceAcc(QueryExecutionFactory qef, MappedConcept<T> mappedConcept, boolean isLeftJoin) {

        Concept concept = mappedConcept.getConcept();
        Query query = ConceptUtils.create();
        //var query = ConceptUtils.createQueryList(concept);

        Agg<T> agg = mappedConcept.getAgg();

        Var  rowId = Var.alloc("rowId");

        // TODO Set up a projection using the grouping variable and the variables referenced by the aggregator
        Set<Var> vars = agg.getDeclaredVars();
        for(Var var : vars) {
            query.getProject().add(var);
        }
        //query.setQueryResultStar(true);

        var ls = new ListServiceSparqlQuery(sparqlService, query, concept.getVar(), isLeftJoin);
    var result = new ListServiceTransformItem(ls, function(entry) {
        var key = entry.key;

        var bindings = entry.val.getBindings();

        // Clone the bindings to avoid corrupting caches
        bindings = BindingUtils.cloneBindings(bindings);

        // Augment them with a rowId attribute
        BindingUtils.addRowIds(bindings, rowId);

        var acc = agg.createAcc();
        bindings.forEach(function(binding) {
            acc.accumulate(binding);
        });

        var r = {key: key, val: acc};
        return r;
    });

    //var result = this.createLookupServiceAgg(sparqlService, query, concept.getVar(), mappedConcept.getAgg());
    return result;
},

    public static <T> ListService<Concept, T> createListServiceMappedConcept(QueryExecutionFactory qef, MappedConcept<T> mappedConcept, boolean isLeftJoin) {
        ListService<Concept, T> ls = createListServiceAcc(qef, mappedConcept, isLeftJoin);

        // Add a transformer that actually retrieves the value from the acc structure
        ListService<T> result = new ListServiceTransformItem(ls, function(accEntries) {
            var r = accEntries.map(function(accEntry) {
                var s = accEntry.val.getValue();
                return s;
            });

            return r;
        });

        return result;
    }
};

}
