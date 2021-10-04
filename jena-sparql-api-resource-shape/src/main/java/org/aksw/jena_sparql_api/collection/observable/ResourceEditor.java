package org.aksw.jena_sparql_api.collection.observable;

import org.aksw.commons.collection.observable.ObservableSet;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

/**
 * A view over a resource being edited in a {@link GraphChange}.
 *
 * @author raven
 *
 */
public class ResourceEditor {
    protected Node sourceNode;
    protected GraphChange graph;

    public ObservableSet<Triple> getExistingTriples() {
        return null;
    }

    public ObservableSet<Triple> getInferredTriples() {
        return null;
    }

    public ObservableSet<Triple> getNewPhysicalTriples() {
        return null;
    }


}
