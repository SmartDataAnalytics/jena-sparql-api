package org.aksw.jena_sparql_api.mapper.model;

import java.util.Collection;

import org.apache.jena.rdf.model.Resource;

public interface TypeDecider
	extends ShapeExposable
{
    Collection<Class<?>> getApplicableTypes(Resource subject);
    void writeTypeTriples(Resource outResource, Object entity);
}
