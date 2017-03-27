package org.aksw.jena_sparql_api.utils.transform;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.graph.NodeTransform;

/**
 * Transformer that does nothing, but collects all encountered nodes
 *
 * @author raven
 */
public class NodeTransformCollectNodes
    implements NodeTransform
{
    public Set<Node> nodes = new HashSet<Node>();

    @Override
    public Node apply(Node node) {
        nodes.add(node);
        return node;
    }

    public Set<Node> getNodes() {
        return nodes;
    }
}
