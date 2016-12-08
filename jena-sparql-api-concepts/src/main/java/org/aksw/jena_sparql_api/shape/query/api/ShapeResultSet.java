package org.aksw.jena_sparql_api.shape.query.api;

import java.util.stream.Stream;

import org.aksw.jena_sparql_api.shape.ResourceShape;

public interface ShapeResultSet {
    /**
     * Provides information about how the stream was created and enables using
     * the current set of resources as a starting point for sub queries.
     *
     *
     *
     * @return
     */
    ShapeContext getShapeContext();

    Stream<ResourceShape> stream();
    //CountInfo count()
}
