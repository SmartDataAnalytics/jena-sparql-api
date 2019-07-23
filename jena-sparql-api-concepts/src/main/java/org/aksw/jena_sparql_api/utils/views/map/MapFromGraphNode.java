package org.aksw.jena_sparql_api.utils.views.map;

import java.util.AbstractMap;
import java.util.Set;

import org.apache.jena.graph.Node;

/**
 * 
 * Map from nodes that represent entries:
 * 
 * Pattern:
 * ?e :k ?k ; :v ?v
 * 
 * 
 * @author raven
 *
 */
public class MapFromGraphNode
	extends AbstractMap<Node, Node>
{

	@Override
	public Set<Entry<Node, Node>> entrySet() {
		// TODO Auto-generated method stub
		return null;
	}

}
