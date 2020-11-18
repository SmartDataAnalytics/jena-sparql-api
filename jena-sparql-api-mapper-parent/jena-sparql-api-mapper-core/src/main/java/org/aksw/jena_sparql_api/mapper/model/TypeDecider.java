package org.aksw.jena_sparql_api.mapper.model;

import java.util.Collection;

import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.apache.jena.rdf.model.Resource;

public interface TypeDecider
    extends ShapeExposable
{
    // TODO Change to a quad relation that maps resources to the set of triples from which their type
    // is derived
    void exposeShape(ResourceShapeBuilder rsb, Class<?> clazz);
    Collection<Class<?>> getApplicableTypes(Resource subject);

    // TODO It must be possible to ask whether a TypeDecider supports a given class
    void writeTypeTriples(Resource outResource, Class<?> clazz); //Object entity);
}
