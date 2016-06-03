package org.aksw.jena_sparql_api.utils.model;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public class ResourceUtils {
    public static void addLiteral(Resource r, Property p, Object o) {
        if(o != null) {
            r.addLiteral(p, o);
        }
    }

    public static void addProperty(Resource r, Property p, RDFNode o) {
        if(o != null) {
            r.addProperty(p, o);
        }
    }
}
