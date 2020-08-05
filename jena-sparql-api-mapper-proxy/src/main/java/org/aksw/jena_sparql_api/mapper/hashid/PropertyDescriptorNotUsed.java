package org.aksw.jena_sparql_api.mapper.hashid;

import org.aksw.jena_sparql_api.mapper.hashid.Metamodel.PropertyDescCollection;
import org.apache.jena.rdf.model.RDFNode;

import com.google.common.collect.Multimap;

/**
 * Sketch of a model where for a given rdf property there can be multiple getter/setter types
 * assigned to it - ever for different datatypes
 *
 * @author raven
 *
 */
public class PropertyDescriptorNotUsed {
    String iri;

    /**
     * Methods grouped by their name.
     * This allows getFoo and setFoo go into one group and getFoos and setFoos into another.
     *
     *
     */
    protected Multimap<String, MethodGroup> methodGroup;

    public String getIri() {
        return iri;
    }

    public boolean isInverse() {
        return false;
    }

    public boolean isCollection() {
        return false;
    }

    public boolean isHashId() {
        return false;
    }

    public Object getValue(RDFNode node) {
        return null;
    }

    public PropertyDescCollection asCollectionProperty() {
        return null;
    }
}