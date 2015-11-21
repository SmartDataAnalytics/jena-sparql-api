package org.aksw.jena_sparql_api.mapper.model;

import org.aksw.jena_sparql_api.concepts.Relation;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;

public interface RdfProperty {

    /**
     * Get the RdfClass of the property value
     *
     * @return
     */
    RdfType getTargetRdfType();

    /**
     * The name of the property
     *
     * @return
     */
    String getName();


    /**
     *
     *
     */
    Relation getRelation();


    /**
     * Write the property value of the given object as RDF
     *
     * @param obj
     * @param outputGraph
     */
    void writePropertyValue(Graph out, Object obj, Node subject);

    /**
     * Read the property value from a given RDF graph
     *
     * @param graph
     * @param subject
     * @return
     */
    Object readPropertyValue(Graph graph, Node subject);
}
