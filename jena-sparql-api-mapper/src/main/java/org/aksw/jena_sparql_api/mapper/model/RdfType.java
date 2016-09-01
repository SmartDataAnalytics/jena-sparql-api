package org.aksw.jena_sparql_api.mapper.model;

import java.util.function.Consumer;

import org.aksw.jena_sparql_api.mapper.context.RdfEmitterContext;
import org.aksw.jena_sparql_api.mapper.context.RdfPersistenceContext;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

/**
 * Base class for operations for mapping java objects of *a specific* class to and from sets of triples.
 * 
 * 
 * <ul>
 *   <li>fetch data from a remote store</li>
 *   <li>instanciate a corresponding java class (can be a proxy implementing appropriate interfaces)</li>
 *   <li>set properties on the instance</li>
 * </ul>
 *
 * Hence, an RDF type exposes information for two phases:
 * <ul>
 *   <li>a shape object that identifies the sub graph relevant for initializing properties</li>
 *   <li>a function that sets properties on an object from that sub graph</li>
 *   <li>a function that converts the state of an object to RDF in regard to this class</li>
 * </ul>
 * can expose information for two phases  a 'shape' of triples
 *
 *
 * @author raven
 *
 */
public interface RdfType
{
    /**
     * Fetch the necessary data for instanciating a Java object that corresponds
     * to this type.
     *
     * @param qef
     * @return
     */


    /**
     * Get the type factory that was used to create this RdfType
     *
     */
    RdfTypeFactory getTypeFactory();


    /**
     * Return the Java class corresponding to this type
     * (maybe it should be Type instead of Class)
     * @return
     */
    Class<?> getEntityClass();

    //EntityOps getEntityOps();
    
    /**
     * Return the root node that corresponds to the given object in regard to this RdfType.
     * In the case of classes, this is an IRI node, whereas for literals this is
     * either a plain or typed literal node.
     *
     * Note, that the following condition is expected to hold:
     *
     * getRootNode(createJavaObject(node)).equals(node)
     *
     * For types mapping to plain literals, this method should (must?) never return null.
     * Note that certain Java types may not have capabilities assigned for returning a node for a given object.
     * In this case, the result will be null.
     * 
     *
     * @param obj
     * @return
     */
    Node getRootNode(Object obj); // TODO May need to add entity manager context argument

    /**
     * Create an empty java object (i.e. no properties set) based on the given
     * node.
     * In the case of primitive types (e.g. String, Long, etc), the object will already carry the correct value.
     * In the case of classes,
     *   the result may either be a Java object regardless of the node argument,
     *   or a proxy to such java object that holds the node
     *
     *
     *
     * @param node
     * @return
     */
    Object createJavaObject(Node node); // TODO May need to add entity manager context argument


    // boolean isHydrated(Object bean)

    /**
     * A simple type is a type that does not need to emit any triples, i.e.
     * a simple type can de/serialize java objects to and from Node objects.
     *
     * @return
     */
    boolean isSimpleType();


    void exposeShape(ResourceShapeBuilder rsb); // Alternative: ResourceShapeBuilder build();


    // TODO It seems it should be this way: persistenceContext.populateEntity(entity, graph, rdfType),
    void populateEntity(RdfPersistenceContext persistenceContext, Object entity, Node subject, Graph inGraph, Consumer<Triple> sink); //, Node g, Node s);

    // These two methods only make sense on classes; but not on primitive types ; maybe move down in the type hierarchy.
//    void populateEntity(RdfPopulationContext populationContext, Object bean, DatasetGraph datasetGraph); //, Node g, Node s);


    //DatasetGraph createDatasetGraph(Object obj, Node g);
    // RdfPersistenceContext persistenceContext, 
    void emitTriples(RdfEmitterContext emitterContext, Object entity, Node subject, Consumer<Triple> sink);
}
