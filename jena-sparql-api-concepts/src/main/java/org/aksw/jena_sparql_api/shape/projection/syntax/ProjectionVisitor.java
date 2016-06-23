package org.aksw.jena_sparql_api.shape.projection.syntax;

public interface ProjectionVisitor<T> {
    T visit(ProjectionGroup proj);
}
