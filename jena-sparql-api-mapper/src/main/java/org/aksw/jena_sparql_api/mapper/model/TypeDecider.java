package org.aksw.jena_sparql_api.mapper.model;

import java.util.Collection;

import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.apache.jena.rdf.model.Resource;

public interface TypeDecider
	extends ShapeExposable
{
	void exposeShape(ResourceShapeBuilder rsb, Class<?> clazz);
    Collection<Class<?>> getApplicableTypes(Resource subject);
    void writeTypeTriples(Resource outResource, Object entity);
}
