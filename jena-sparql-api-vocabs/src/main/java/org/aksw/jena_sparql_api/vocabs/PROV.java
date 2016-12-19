package org.aksw.jena_sparql_api.vocabs;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Provenance Ontology.
 *
 * There is probably some class like this one out there as a maven dep for provo.
 * Once we find it, we can make this class obsolete.
 *
 * @author raven
 *
 */
public class PROV {
    public static final String ns = "http://www.w3.org/ns/prov#";

    public static Property property(String local) {
        return ResourceFactory.createProperty(ns + local);
    }

    public static final Property hadPrimarySource = property("hadPrimarySource");
    public static final Property atTime = property("atTime");
    public static final Property startedAtTime = property("startedAtTime");
    public static final Property endAtTime = property("endAtTime");
    public static final Property wasGeneratedBy = property("wasGeneratedBy");
    public static final Property wasAssociatedWith = property("wasAssociatedWith");

}
