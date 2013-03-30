package org.aksw.jena_sparql_api.utils;


import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import org.aksw.commons.collections.MapUtils;

import java.util.*;

public class QuadUtils
{

	/**
	 * Substitutes the keys in the map
	 *
	 * @param <K>
	 * @param <V>
	 * @param original
	 * @param map
	 * @return
	 */
	public static <K, V> Map<K, V> copySubstitute(Map<K, V> original, Map<K, K> map)
	{
		Map<K, V> result = new HashMap<K, V>();
		for(Map.Entry<K, V> entry : original.entrySet()) {
			result.put(MapUtils.getOrElse(map, entry.getKey(), entry.getKey()), entry.getValue());
		}

		return result;
	}



	public static Quad copySubstitute(Quad quad, Map<? extends Node, ? extends Node> map)
	{
		return new Quad(
				MapUtils.getOrElse(map, quad.getGraph(), quad.getGraph()),
				MapUtils.getOrElse(map, quad.getSubject(), quad.getSubject()),
				MapUtils.getOrElse(map, quad.getPredicate(), quad.getPredicate()),
				MapUtils.getOrElse(map, quad.getObject(), quad.getObject()));
	}

	/**
	 * Create a quad from an array
	 * @param nodes
	 * @return
	 */
	public static Quad create(Node[] nodes)
	{
		return new Quad(nodes[0], nodes[1], nodes[2], nodes[3]);
	}

	public static Node getNode(Quad quad, int index) {
		switch(index) {
		case 0: return quad.getGraph();
		case 1: return quad.getSubject();
		case 2: return quad.getPredicate();
		case 3: return quad.getObject();
		default: throw new IndexOutOfBoundsException("Index: " + index + " Size: " + 4);
		}
	}

	public static Node substitute(Node node, Binding binding) {
		Node result = node;

		if(node.isVariable()) {
			result = binding.get((Var)node);
			if(result == null) {
				throw new RuntimeException("Variable " + node + "not bound");
			}
		}

		return result;
	}

	public static Quad copySubstitute(Quad quad, Binding binding)
	{
		return new Quad(
				substitute(quad.getGraph(), binding),
				substitute(quad.getSubject(), binding),
				substitute(quad.getPredicate(), binding),
				substitute(quad.getObject(), binding));
	}


	/*
	public static QuadPattern copySubstitute(QuadPattern quadPattern, Binding map)
	{
		map.ge
	}*/

	public static QuadPattern copySubstitute(QuadPattern quadPattern, Map<? extends Node, ? extends Node> map)
	{
		QuadPattern result = new QuadPattern();
		for(Quad quad : quadPattern) {
			result.add(copySubstitute(quad, map));
		}

		return result;
	}





	public static Quad listToQuad(List<Node> nodes) {
		return new Quad(nodes.get(0), nodes.get(1), nodes.get(2), nodes.get(3));
	}

	public static List<Node> quadToList(Quad quad)
	{
		List<Node> result = new ArrayList<Node>();
		result.add(quad.getGraph());
		result.add(quad.getSubject());
		result.add(quad.getPredicate());
		result.add(quad.getObject());

		return result;
	}

	public static Set<Var> getVarsMentioned(QuadPattern quadPattern)
	{
		Set<Var> result = new HashSet<Var>();
		for(Quad quad : quadPattern) {
			result.addAll(getVarsMentioned(quad));
		}

		return result;
	}


	public static Set<Var> getVarsMentioned(Quad quad)
	{
		return getVarsMentioned(quadToList(quad));
	}

	public static Set<Var> getVarsMentioned(Iterable<Node> nodes)
	{
		Set<Var> result = new HashSet<Var>();
		for (Node node : nodes) {
			if (node.isVariable()) {
				result.add((Var)node);
			}
		}

		return result;
	}

	public static Map<Node, Node> getVarMapping(Quad a, Quad b)
	{
		List<Node> nAs = quadToList(a);
		List<Node> nBs = quadToList(b);

		Map<Node, Node> result = new HashMap<Node, Node>();
		for(int i = 0; i < 4; ++i) {
			Node nA = nAs.get(i);
			Node nB = nBs.get(i);

			if(nA.isVariable()) {
				Map<Node, Node> newEntry = Collections.singletonMap(nA, nB);

				//MapUtils.isCompatible(result, newEntry);

				result.putAll(newEntry);
			} else {
				if(!nA.equals(nB)) {
					return null;
				}
			}
		}

		return result;
	}
}
