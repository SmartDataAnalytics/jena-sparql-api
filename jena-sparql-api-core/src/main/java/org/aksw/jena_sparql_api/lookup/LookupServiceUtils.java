package org.aksw.jena_sparql_api.lookup;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.mapper.FunctionResultSetAggregate;
import org.aksw.jena_sparql_api.mapper.MappedConcept;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.expr.NodeValue;

public class LookupServiceUtils {
    public static <T> LookupService<Node, T> createLookupService(QueryExecutionFactory sparqlService, MappedConcept<T> mappedConcept) {
        
        Concept concept = mappedConcept.getConcept();
        Query query = concept.asQuery();
        query.setQueryResultStar(true);
        
        // TODO Set up a projection using the grouping variable and the variables referenced by the aggregator
        
        LookupService<Node, ResultSetPart> base = new LookupServiceSparqlQuery(sparqlService, query, concept.getVar());
        
        FunctionResultSetAggregate<T> transform = FunctionResultSetAggregate.create(mappedConcept.getAggregator());
        
        LookupService<Node, T> result = LookupServiceTransformValue.create(base, transform);
        return result;
    }
}
