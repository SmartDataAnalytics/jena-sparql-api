package org.aksw.jena_sparql_api.rdf_stream;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Iteration vocabulary
 *
 * Defines generic terms for looping
 *
 * Users should only fall back to these terms if they know of no better vocabulary that fits their use.
 *
 * Suggested order (based on gut feeling)
 * experiment > job > step > run > phase > cycle
 *
 * @author raven
 *
 */
public class IV {
    public static final String ns = "http://iv.aksw.org/vocab#";

    public static Resource resource(String local) { return ResourceFactory.createResource(ns + local); }
    public static Property property(String local) { return ResourceFactory.createProperty(ns + local); }

    public static final Property experiment = property("experiment");
    public static final Property job = property("job");
    public static final Property step = property("step");
    public static final Property run = property("run");
    public static final Property phase = property("phase");
    public static final Property cycle = property("cycle");

    // Overall id of an item in a stream
    public static final Property item = property("item");

}
