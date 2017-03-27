package org.aksw.jena_sparql_api.mapper.model;

import org.aksw.jena_sparql_api.mapper.impl.type.EntityFragment;
import org.aksw.jena_sparql_api.mapper.impl.type.PathFragment;
import org.aksw.jena_sparql_api.mapper.impl.type.ResourceFragment;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

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
	
	//EntityOps getEntityOps();


    /**
     * Get the type factory that was used to create this RdfType
     *
     */
    //RdfTypeFactory getTypeFactory();


    /**
     * Return the Java class corresponding to this type
     * (maybe it should be Type instead of Class)
     * @return
     */
    Class<?> getEntityClass();

    
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
     * Flag to indicate whether entities created from this mapping have their own identity.
     * If not, ids are usually derived from the parent object
     *  
     * @return
     */
    boolean hasIdentity();
    
    /**
     * Extract a Java (literal) object from a given node.
     * 
     * Note: Creating a *non-primitive* java object is not a concern of RdfType which only *MAPS*
     * between a java object and its corresponding triples.
     * The reason is, that via the RdfType's entity class the association to a newInstance method can be
     * indirectly made on the outside, without RdfType having to be aware of it. 
     * Also, an RdfType reading a collection may be capable of reading and writing to any collection type, regardless
     * of the concrete sub-type (list, set, etc). 
     *
     * 
     * 
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
    //Object createJavaObject(Node node); // TODO May need to add entity manager context argument
    // TODO This is the responsibility of the type decider
    Object createJavaObject(RDFNode r);

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
    //void populateEntity(RdfPersistenceContext persistenceContext, Object entity, Node subject, Graph inGraph, Consumer<Triple> sink); //, Node g, Node s);

    // These two methods only make sense on classes; but not on primitive types ; maybe move down in the type hierarchy.
//    void populateEntity(RdfPopulationContext populationContext, Object bean, DatasetGraph datasetGraph); //, Node g, Node s);

    /**
     * 
     * 
     * @param out
     * @param priorState
     * @param entity
     */
    void exposeFragment(ResourceFragment out, Resource priorState, Object entity);
    
    /**
     * Populates an entity from a resource RDF graph that should match the
     * exposed shape.
     * 
     * 
     * 
     * @param inout
     * @param shape
     * @param entity
     */
    EntityFragment populate(Resource shape, Object entity);

    
    PathFragment resolve(String propertyName);

    //DatasetGraph createDatasetGraph(Object obj, Node g);
    // RdfPersistenceContext persistenceContext, 
    //void emitTriples(RdfEmitterContext emitterContext, Object entity, Node subject, Graph shapeGraph, Consumer<Triple> sink);

//    void exposeTypeDeciderShape(ResourceShapeBuilder rsb);
//    Collection<RdfType> getApplicableTypes(Resource resource);
}
