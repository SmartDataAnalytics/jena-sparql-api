package org.aksw.jena_sparql_api.constraint.api;

import org.aksw.jena_sparql_api.utils.NodeUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;

/**
 * Wrap a Node and its corresponding NodeValue as a comparable object.
 * Allows for use in guava's RangeSet.
 *
 * @author raven
 *
 */
public class NodeWrapper
    implements Comparable<NodeWrapper>
{
    protected Node node;
    protected NodeValue nodeValue;

    protected NodeWrapper(Node node, NodeValue nodeValue) {
        super();
        this.node = node;
        this.nodeValue = nodeValue;
    }

    public Node getNode() {
        return node;
    }

    public NodeValue getNodeValue() {
        return nodeValue;
    }

    public static NodeWrapper wrap(Node node) {
        return new NodeWrapper(node, NodeValue.makeNode(node));
    }

    public static NodeWrapper wrap(NodeValue nodeValue) {
        return new NodeWrapper(nodeValue.asNode(), nodeValue);
    }


    @Override
    public int compareTo(NodeWrapper other) {
        return NodeUtils.compareAlways(node, other.node);
    }

    @Override
    public String toString() {
        return node.toString();
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((node == null) ? 0 : node.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NodeWrapper other = (NodeWrapper) obj;
        if (node == null) {
            if (other.node != null)
                return false;
        } else if (!node.equals(other.node))
            return false;
        return true;
    }
}
