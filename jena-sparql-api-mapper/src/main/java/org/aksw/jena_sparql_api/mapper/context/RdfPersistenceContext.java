package org.aksw.jena_sparql_api.mapper.context;

import org.aksw.jena_sparql_api.mapper.impl.engine.EntityGraphMap;
import org.aksw.jena_sparql_api.mapper.model.RdfType;
import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;
import org.aksw.jena_sparql_api.util.frontier.Frontier;
import org.apache.jena.graph.Node;

/**
 * TODO Maybe this could subclass from BeanFactory?
 *
 * A population context holds information needed to
 * populate a Java object graph.
 *
 * For *non-primitive types*, a population context
 * must yield the same Java object for a given rdfType and id.
 *
 * TODO Clarify for whether the condition should also hold for primitive types
 *
 *
 *
 * @author raven
 *
 */
public interface RdfPersistenceContext
{
    /**
     * Return an Rdf type for a given Java class
     *
     * @param clazz
     * @return
     */
    //RdfType forJavaType(Class<?> clazz);
    //RdfTypeFactory getTypeFactory();

    /**
     * Return an RDF node for this entity
     *
     * @param entity
     * @return
     */
//	Node getRootNode(Object entity);
//	void setRootNode(Object entity, Node node);

    RdfTypeFactory getTypeFactory();

    /**
     * Return either an existing Java object for the given node under a given rdfType,
     * or return a fresh, unpopulated Java object for that given rdfType
     *
     * @param rdfType
     * @param node
     * @return
     */
    Object entityFor(TypedNode typedNode);


    /**
     * Only returns a non-null result if there already exists an entity for the typedNode
     *
     * @param typedNode
     * @return
     */
    Object getEntity(TypedNode typedNode);


    @Deprecated
    Node getRawRootNode(Object entity);

    // TODO: Not sure if the typeFactory should be a field of the persistence context
    // RdfTypeFactory typeFactory, 
    Node getRootNode(Object entity);
    
    void put(Node node, Object entity);
    
    /**
     * Whether the given bean is managed by this context; i.e.
     * whether the bean has bean created with .objectFor()
     *
     * @param bean
     * @return
     */
    boolean isManaged(Object entity);

    /**
     * Whether according to this context the given bean has been populated
     *
     * @param bean
     * @return
     */
    boolean isPopulated(Object entity);


    EntityGraphMap getEntityGraphMap();

    /**
     * The set of not yet populated type/node pairs.
     * @return
     */
    Frontier<TypedNode> getFrontier();
    // EntityGraphMap entityGraphMap = new EntityGraphMap();

    //boolean setPopulated(O)

    
    /**
     * Request to fill out an entity's property value for a given subject and type. 
     * 
     * @param entity
     * @param propertyName
     * @param subject
     * @param rdfType
     */
    void requestResolution(Object entity, String propertyName, Node subject, RdfType rdfType);
    
    
    
    /**
     * Whether according to the context the bean has been populated
     *
     * @param bean
     * @return
     */
    //boolean isPopulated(Object bean);

    /**
     * Notify the context that for the given object and property,
     *
     * @param obj
     * @param propertyName
     */
    //void addLooseEnd(Object obj, String propertyName);
}
