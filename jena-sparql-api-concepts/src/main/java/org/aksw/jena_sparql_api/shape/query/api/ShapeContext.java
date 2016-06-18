package org.aksw.jena_sparql_api.shape.query.api;

import org.aksw.jena_sparql_api.shape.syntax.Element;
import org.aksw.jena_sparql_api.shape.syntax.ShapeQuery;

/**
 * Provides information about the set of values for a given property
 * @author raven
 *
 */
public interface ShapeContext {
    ShapeQueryExecutionFactory getShapeExecutionFactory();
    Element getBaseConcept();
    //Stream<ShapeResultSet> execute(ShapeQuery shapeQuery);
    //Shap

    ShapeResultSet execute(ShapeQuery query);

}
