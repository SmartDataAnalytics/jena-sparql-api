package org.aksw.jena_sparql_api.rdf.model.ext.dataset.api;

import java.util.Collection;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.rdf.model.ext.dataset.impl.GraphNameAndNode;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;

public interface NodesInDataset {
    Dataset getDataset();

    /**
     * Add a graph name node pair to this collection.
     *
     * Optional operation. May raise {@link UnsupportedOperationException}.
     *
     * @param graphName
     * @param node
     * @return true if a new entry was added, false otherwise
     */
    boolean add(String graphName, Node node);


    /**
     * Remove a graph name node pair from this collection.
     *
     * Optional operation. May raise {@link UnsupportedOperationException}.
     *
     * @param graphName
     * @param node
     * @return true if a new entry was removed, false otherwise
     */
    boolean remove(String graphName, Node node);

    /** A typically immutable collection of the entries */
    Collection<GraphNameAndNode> getGraphNameAndNodes();


    /** Stream the nodes as RDFNodeInDataset instances */
    Stream<RDFNodeInDataset> stream();
}