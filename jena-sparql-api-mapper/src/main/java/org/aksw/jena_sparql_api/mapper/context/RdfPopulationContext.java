package org.aksw.jena_sparql_api.mapper.context;

import com.hp.hpl.jena.graph.Node;

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
public interface RdfPopulationContext
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


    Node getRootNode(Object entity);

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

    //boolean setPopulated(O)

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
