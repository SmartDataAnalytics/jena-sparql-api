package org.aksw.jena_sparql_api.shape.query.api;

import org.aksw.jena_sparql_api.shape.syntax.ShapeQuery;

public interface ShapeQueryExecutionFactory {
    ShapeQueryExecution createShapeExecution(ShapeQuery query);
}
