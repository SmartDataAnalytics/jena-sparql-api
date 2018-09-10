package org.aksw.jena_sparql_api.mapper.model;

import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;

/**
 * An attempt at a populator interface that maps between key-value pairs
 * and RDF data.
 *
 *
 *
 * @author raven
 *
 */
public interface RdfRawPopulator {

	/**
	 * Return the set of bean properties which this populator uses
	 *
	 * @return
	 */
	Set<String> getPropertyNames();


	void exposeShape(ResourceShapeBuilder shapeBuilder);

    /**
     * Emit triples from the object
     *
     * @param obj
     * @param outputGraph
     */
    void emitTriples(Graph out, Map<String, Object> map, Node subject);

    /**
     * Set bean property values from a given subject's RDF graph
     *
     * @param graph
     * @param subject
     * @return
     */
    Map<String, Object> readProperties(Graph graph, Node subject);
}
