package org.aksw.jena_sparql_api.geo;

import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.lookup.LookupServiceTransformValue;
import org.aksw.jena_sparql_api.lookup.LookupServiceUtils;
import org.aksw.jena_sparql_api.mapper.MappedConcept;
import org.apache.jena.graph.Node;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

import com.vividsolutions.jts.geom.Geometry;

public class LookupServiceUtilsGeo {
    public static LookupService<Node, Geometry> createGeoLookupService(SparqlQueryConnection sparqlService, MappedConcept<String> mappedConcept) {

        LookupService<Node, String> ls = LookupServiceUtils.createLookupService(sparqlService, mappedConcept);
        LookupService<Node, Geometry> result = LookupServiceTransformValue.create(ls, FN_ParseWkt.create());

        return result;
    }
}
