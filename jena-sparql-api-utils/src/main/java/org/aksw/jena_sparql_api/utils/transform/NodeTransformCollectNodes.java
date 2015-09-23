package org.aksw.jena_sparql_api.utils.transform;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.graph.NodeTransform;

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
	public Node convert(Node node) {
		nodes.add(node);
		return node;
	}

	public Set<Node> getNodes() {
		return nodes;
	}
}
