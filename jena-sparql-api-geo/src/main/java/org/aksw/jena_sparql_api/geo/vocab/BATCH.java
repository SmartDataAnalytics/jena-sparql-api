package org.aksw.jena_sparql_api.geo.vocab;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class BATCH {
    public static final String ns = "http://aksw.org/batch/";

    public static String getURI() {
        return ns;
    }

    public static Property property(String property) {
        return ResourceFactory.createProperty(ns + property);
    }

    public static final Property locationString = property("locationString");

    public static final Property startTime = property("startTime");
    public static final Property stopTime = property("stopTime");
    public static final Property exitMessage = property("exitMessage");
    public static final Property version = property("version");
    public static final Property endTime = property("endtime");
    public static final Property exitCode = property("exitCode");
    public static final Property exitStatus = property("exitStatus");
    public static final Property status = property("status");
    public static final Property id = property("id");
    public static final Property jobId = property("jobId");

    public static final Property jobExecutionId = property("jobExecutionId");
    public static final Property key = property("key");
    public static final Property value = property("value");
    public static final Property identifying = property("identifying");

    public static final Property hasParam = property("hasParam");
    public static final Property jobConfigurationLocation = property("jobConfigurationLocation");

}
