package org.aksw.jena_sparql_api.shape.projection.syntax;

public interface Projection {
    <T> T accept(ProjectionVisitor<T> visitor);
}
