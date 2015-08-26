package org.aksw.jena_sparql_api.convert;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public interface FN_GenerateResource {
    Resource create(Model model, Resource base, Property property);
}
