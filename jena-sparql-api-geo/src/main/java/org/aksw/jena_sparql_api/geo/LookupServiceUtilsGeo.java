package org.aksw.jena_sparql_api.geo;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.lookup.LookupServiceTransformValue;
import org.aksw.jena_sparql_api.lookup.LookupServiceUtils;
import org.aksw.jena_sparql_api.mapper.FunctionNodeValueToString;
import org.aksw.jena_sparql_api.mapper.MappedConcept;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.vividsolutions.jts.geom.Geometry;

public class LookupServiceUtilsGeo {
    public static LookupService<Node, Geometry> createGeoLookupService(QueryExecutionFactory sparqlService, MappedConcept<String> mappedConcept) {
     
        LookupService<Node, String> ls = LookupServiceUtils.createLookupService(sparqlService, mappedConcept); 
        LookupService<Node, Geometry> result = LookupServiceTransformValue.create(ls, FunctionParseWkt.create());

        return result;
    }
}
