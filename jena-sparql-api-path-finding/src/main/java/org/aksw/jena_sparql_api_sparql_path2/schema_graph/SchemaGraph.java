package org.aksw.jena_sparql_api_sparql_path2.schema_graph;

import java.util.Set;

public interface SchemaGraph<V, S, T> {
    /**
     * For a given data item, find the corresponding set of vertices in the
     * schema graph
     *
     * @param node
     * @return
     */
    Set<S> findCandidateVertices(V node);

    /**
     * Return a description of which (sub-properties) of a resource to retrieve
     * in order to decide its relation to candidate vertices
     *
     * @return
     */
    //ResourceShape getResourceShape();




}
