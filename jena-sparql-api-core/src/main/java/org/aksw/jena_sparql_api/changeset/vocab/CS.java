package org.aksw.jena_sparql_api.changeset.vocab;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * The changeset vocabulary
 *
 * @author raven
 *
 */
public class CS {
    public static final String ns = "http://purl.org/vocab/changeset/schema#";

    public static Resource createResource(String localName) {
        Resource result = ResourceFactory.createResource(ns + localName);
        return result;
    }

    public static Property createProperty(String localName) {
        Property result = ResourceFactory.createProperty(ns + localName);
        return result;
    }

    public static final Resource ChangeSet = createResource("ChangeSet");

    public static final Property addition = createProperty("addition");
    public static final Property changeReason = createProperty("changeReason");
    public static final Property createdDate = createProperty("createdDate");
    public static final Property creatorName = createProperty("creatorName");
    public static final Property precedingChangeSet = createProperty("precedingChangeSet");
    public static final Property removal = createProperty("removal");
    public static final Property statement = createProperty("statement");
    public static final Property subjectOfChange = createProperty("subjectOfChange");

    /*
     *  !!! Warning !!! non-standard extensions below
     */

    /**
     * The graph on which the change was carried out
     */
    //public static final Property sequence = createProperty("sequenceId");
    public static final Property service = createProperty("service");
    public static final Property graph = createProperty("graph");
    public static final Property transaction = createProperty("transaction");
}
