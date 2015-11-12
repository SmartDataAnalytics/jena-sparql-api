package org.aksw.jena_sparql_api.mapper.model;

import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.DatasetGraph;

/**
 * Base class for RDF based types.
 * A type is used to
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
     * Return the Java class corresponding to this type
     * (maybe it should be Type instead of Type)
     * @return
     */
    Class<?> getTargetClass();

    void build(ResourceShapeBuilder rsb); // Alternative: ResourceShapeBuilder build();
    void setValues(Object targetObj, DatasetGraph datasetGraph); //, Node g, Node s);
    DatasetGraph createDatasetGraph(Object obj, Node g);
}
