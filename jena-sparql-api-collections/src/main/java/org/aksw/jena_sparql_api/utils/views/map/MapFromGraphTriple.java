package org.aksw.jena_sparql_api.utils.views.map;

import java.util.AbstractMap;
import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;

/**
 * Simple map based on a set of triples.
 * 
 * Pattern:
 * ?k :p ?v
 * 
 * @author raven
 *
 */
public class MapFromGraphTriple
	extends AbstractMap<Node, Node>
{
	protected Graph graph;
//	protected 

	@Override
	public Set<Entry<Node, Node>> entrySet() {
		//graph.find(s, p, o)
		
		// TODO Auto-generated method stub
		return null;
	}
}
