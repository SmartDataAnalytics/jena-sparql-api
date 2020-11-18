package org.aksw.jena_sparql_api_sparql_path2.schema_graph;

import java.util.Set;

/**
 * RDFS based schema graph implementation
 *
 * rdfs:subClassOf
 * rdfs:domain
 * rdfs:range
 * rdfs:subPropertyOf
 *
 *
 *
 *
 * @author raven
 *
 */
public class SchemaGraphRdfs<S, T> {

    /**
     * Returns all sub-and super classes this vertex corresponds to.
     *
     *
     *
     * @param vertex
     * @return
     */
    Set<S> expandVertex(S vertex) {

        return null;
    }

}

