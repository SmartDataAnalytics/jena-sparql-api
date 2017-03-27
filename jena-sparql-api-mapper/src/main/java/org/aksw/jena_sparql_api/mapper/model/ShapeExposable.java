package org.aksw.jena_sparql_api.mapper.model;

import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;

public interface ShapeExposable {
	void exposeShape(ResourceShapeBuilder rsb);
}
