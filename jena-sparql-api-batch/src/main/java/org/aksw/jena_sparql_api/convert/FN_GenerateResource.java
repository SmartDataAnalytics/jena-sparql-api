package org.aksw.jena_sparql_api.convert;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public interface FN_GenerateResource {
    Resource create(Model model, Resource base, Property property);
}
