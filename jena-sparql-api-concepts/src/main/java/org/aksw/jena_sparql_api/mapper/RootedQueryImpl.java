package org.aksw.jena_sparql_api.mapper;

import org.apache.jena.graph.Node;

public class RootedQueryImpl
    implements RootedQuery
{
    protected Node rootNode;
    protected PartitionedQuery partitionedQuery;

    public RootedQueryImpl(Node rootNode, PartitionedQuery partitionedQuery) {
        super();
        this.rootNode = rootNode;
        this.partitionedQuery = partitionedQuery;
    }

    @Override
    public Node getRootNode() {
        return rootNode;
    }

    @Override
    public PartitionedQuery getPartitionedQuery() {
        return partitionedQuery;
    }
}
