package org.aksw.jena_sparql_api.mapper.model;

import org.aksw.commons.beans.model.PropertyOps;
import org.apache.jena.graph.Node;

/**
 * Base class for RdfPopulators that operate on a single bean property
 * @author raven
 *
 */
public interface RdfMapperProperty
	extends RdfMapper
{

    /**
     * Get the RdfClass of the property value
     *
     * @return
     */
    //RdfType getTargetRdfType();

    /**
     * The name of the property
     *
     * @return
     */
    //String getPropertyName();
    
    PropertyOps getPropertyOps();


    /**
     *
     *
     */
//    Relation getRelation();


    /**
     * Read the property value from a given RDF graph
     *
     * @param graph
     * @param subject
     * @return
     */
    //Object readPropertyValue(Graph graph, Node subject);

    //getPopulator();
    //getEmitter();
    
    Node getTargetNode(String subjectUri, Object entity);

}
