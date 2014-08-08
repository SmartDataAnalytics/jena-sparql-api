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
    public static LookupService<Node, Geometry> createGeoLookupService(QueryExecutionFactory sparqlService, MappedConcept mappedConcept) {
     
        LookupService<Node, NodeValue> ls = LookupServiceUtils.createGeoLookupService(sparqlService, mappedConcept);        
        LookupService<Node, String> a = LookupServiceTransformValue.create(ls, FunctionNodeValueToString.fn);
        LookupService<Node, Geometry> result = LookupServiceTransformValue.create(a, FunctionParseWkt.create());

        return result;
    }
}
