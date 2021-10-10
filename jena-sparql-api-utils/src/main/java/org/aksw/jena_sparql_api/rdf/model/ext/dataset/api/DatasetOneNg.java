package org.aksw.jena_sparql_api.rdf.model.ext.dataset.api;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Quad;

/**
 * Evolving.
 *
 * Interface for dataset implementations that only allow for a single named graph.
 * Typical use is for event streams where each event is encoded as a named graph.
 *
 * Whether a default graph may exist instead of or in addition to a named graph
 * is (currently) unspecified.
 *
 * @author raven
 *
 */
public interface DatasetOneNg
    extends Dataset
{
    String getGraphName();

    default Model getModel() {
        String g = getGraphName();
        Model result = Quad.isDefaultGraph(NodeFactory.createURI(g))
            ? getDefaultModel()
            : getNamedModel(g);

        return result;
    }
}
