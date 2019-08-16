package org.aksw.jena_sparql_api.mapper.proxy;

import java.util.Collection;

import org.apache.jena.rdf.model.Resource;

/**
 * A type decider manages a set of associations between descriptions of resources and Java classes.
 * In the simplest case, these descriptions are based on the rdf:type property.
 * 
 * On the one hand, a type decider yields for a given resource a set of Java classes it can be viewed with.
 * While not mandatory, a common use case is for these classes to be subclasses of {@link Resource}
 * that can be used directly as views using resource.as(viewClass).
 * On the other hand, a type decider can enrich a resource with information from which its assocation
 * to one or more Java classes can be inferred. 
 * 
 * 
 * @author raven
 *
 */
public interface TypeDecider
//    extends ShapeExposable
{
//    void exposeShape(ResourceShapeBuilder rsb, Class<?> clazz);
	/**
	 * Yield the classes associated with the resource's description
	 * 
	 * @param subject
	 * @return
	 */
    Collection<Class<?>> getApplicableTypes(Resource subject);
   
    /**
     * Add triples to the given resource that associates
     * it with the provided class.
     * 
     * @param outResource
     * @param clazz
     */
    void writeTypeTriples(Resource outResource, Class<?> clazz); //Object entity);
}
