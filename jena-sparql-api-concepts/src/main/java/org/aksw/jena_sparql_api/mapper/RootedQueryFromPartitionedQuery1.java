package org.aksw.jena_sparql_api.mapper;

import org.apache.jena.graph.Node;

/**
 * Treat the sole partition variable of a {@link PartitionedQuery1} as the root node.
 *
 * @author raven
 *
 */
public class RootedQueryFromPartitionedQuery1
    implements RootedQuery
{
    protected PartitionedQuery1 partitionedQuery1;

    public RootedQueryFromPartitionedQuery1(PartitionedQuery1 partitionedQuery1) {
        super();
        this.partitionedQuery1 = partitionedQuery1;
    }

    @Override
    public Node getRootNode() {
        return partitionedQuery1.getPartitionVar();
    }

    @Override
    public ObjectQuery getObjectQuery() {
        // TODO Have the object query wrapper delegate every call to partitionedQuery.getQuery()
        return new ObjectQueryFromQuery(partitionedQuery1.getQuery());
    }
}
