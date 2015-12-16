package org.aksw.jena_sparql_api.mapper.model;

import java.util.Set;

import org.aksw.jena_sparql_api.mapper.context.RdfEmitterContext;
import org.aksw.jena_sparql_api.mapper.context.RdfPersistenceContext;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.aksw.jena_sparql_api.util.frontier.Frontier;
import org.apache.jena.atlas.lib.Sink;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

/**
 * RdfPopulators map bean properties to triples and vice versa.
 * In general RdfPopulators can implement arbitrary mappings between
 * bean properties and RDF triples, i.e.
 * beanProperties:triples -- 1:n, m:n, m:1
 *
 * Note that an RDF populator only populates the *immediate* properties.
 * Scheduling Lazy / Eager fetching is NOT up to the populator.
 *
 * @author raven
 *
 */
public interface RdfPopulator {

    /**
     * Return the set of bean properties which this populator uses.
     *
     *
     * @return
     */
    Set<String> getPropertyNames();


    /**
     * Expose SPARQL patterns that identify the set of triples
     * that are needed to populate the *immediate* properties
     *
     *
     * @param shapeBuilder
     */
    void exposeShape(ResourceShapeBuilder shapeBuilder);


    /**
     * Emit triples from the object
     *
     * @param outGraph
     * @param bean
     * @param subject
     */
    void emitTriples(RdfPersistenceContext persistenceContext, RdfEmitterContext emitterContext, Graph outGraph, Object bean, Node subject);

    /**
     * Set bean property values from a given subject's RDF graph
     *
     * @param graph
     * @param subject
     * @return
     */
    void populateEntity(RdfPersistenceContext persistenceContext, Object bean, Graph graph, Node subject, Sink<Triple> outSink);
}
