package org.aksw.jena_sparql_api.rdf.model.ext.dataset.impl;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.rdf.model.ext.dataset.api.NodesInDataset;
import org.aksw.jena_sparql_api.rdf.model.ext.dataset.api.RDFNodeInDataset;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;

/**
 * A collection of nodes in specific named graphs in a dataset.
 * This implementation is backed by a Set&lt;GraphNameAndNode&gt;.
 *
 */
public class NodesInDatasetImpl implements NodesInDataset {
    protected Dataset dataset;
    protected Set<GraphNameAndNode> graphNameAndNodes;

    public NodesInDatasetImpl(Dataset dataset) {
        this(dataset, new LinkedHashSet<>());
    }

    public NodesInDatasetImpl(Dataset dataset, Set<GraphNameAndNode> node) {
        super();
        this.dataset = dataset;
        this.graphNameAndNodes = node;
    }

    @Override
    public boolean add(String graphName, Node node) {
        return graphNameAndNodes.add(new GraphNameAndNode(graphName, node));
    }

    @Override
    public boolean remove(String graphName, Node node) {
        return graphNameAndNodes.remove(new GraphNameAndNode(graphName, node));
    }

    @Override
    public Dataset getDataset() {
        return dataset;
    }

    public Set<GraphNameAndNode> getGraphNameAndNodes() {
        return graphNameAndNodes;
    }


    /** Stream the nodes as RDFNodeInDataset instances */
    @Override
    public Stream<RDFNodeInDataset> stream() {
        return graphNameAndNodes.stream().map(gn -> RDFNodeInDataset.create(dataset, gn.getGraphName(), gn.getNode()));
    }

    @Override
    public String toString() {
        return "graphNameAndNodes=" + graphNameAndNodes + ", datasetSize=" + Streams.stream(dataset.asDatasetGraph().find()).count();
    }
}