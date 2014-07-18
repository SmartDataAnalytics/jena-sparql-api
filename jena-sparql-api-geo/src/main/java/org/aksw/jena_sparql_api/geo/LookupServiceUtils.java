package org.aksw.jena_sparql_api.geo;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.lookup.LookupServiceSparqlQuery;
import org.aksw.jena_sparql_api.lookup.LookupServiceTransformValue;
import org.aksw.jena_sparql_api.lookup.ResultSetPart;
import org.aksw.jena_sparql_api.sparql_path.core.domain.Concept;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.vividsolutions.jts.geom.Geometry;

public class LookupServiceUtils {
    public static LookupService<Node, Geometry> createGeoLookupService(QueryExecutionFactory sparqlService, MappedConcept mappedConcept) {
     
        Concept concept = mappedConcept.getConcept();
        Query query = concept.asQuery();
        query.setQueryResultStar(true);
        
        // TODO Set up a projection using the grouping variable and the variables referenced by the aggregator
        
        LookupService<Node, ResultSetPart> base = new LookupServiceSparqlQuery(sparqlService, query, concept.getVar());
        
        FunctionResultSetAggregate transform = new FunctionResultSetAggregate(mappedConcept.getAggregator());
        
        LookupService<Node, NodeValue> ls = LookupServiceTransformValue.create(base, transform);
        
        LookupService<Node, String> a = LookupServiceTransformValue.create(ls, FunctionNodeValueToString.fn);
        LookupService<Node, Geometry> result = LookupServiceTransformValue.create(a, FunctionParseWkt.create());

        return result;
    }
}
