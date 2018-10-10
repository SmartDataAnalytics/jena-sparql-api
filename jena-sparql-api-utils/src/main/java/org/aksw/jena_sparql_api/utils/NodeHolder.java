package org.aksw.jena_sparql_api.utils;

import java.util.Objects;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;

/**
 * A wrapper for {@link Node} that implements Comparable using NodeValue.compareAlways.
 * Allows usage of Node e.g. with Guava's Range class.
 * 
 * @author Claus Stadler, Oct 10, 2018
 *
 */
public class NodeHolder
	implements Comparable<NodeHolder>
{
	protected Node node;

	public NodeHolder(Node node) {
		this.node = node;
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
		NodeHolder other = (NodeHolder) obj;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		return true;
	}



	@Override
	public int compareTo(NodeHolder o) {
		NodeValue a = NodeValue.makeNode(node);
		NodeValue b = NodeValue.makeNode(o.node);
		int result = NodeValue.compareAlways(a, b);
		return result;
	}

	@Override
	public String toString() {
		return Objects.toString(node);
	}
}
