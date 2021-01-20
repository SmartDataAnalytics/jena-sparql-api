package org.aksw.jena_sparql_api.analytics;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.mapper.Accumulator;
import org.aksw.jena_sparql_api.util.graph.alg.GraphSuccessorFunction;
import org.aksw.jena_sparql_api.util.graph.alg.NaiveLCAFinder;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

/**
 * Note: The backing graph must form a tree (not a dag): There must be at most a single lca
 * for any two nodes. 
 * 
 * @author raven
 *
 */
public class AccRdfLiteralDatatypeTypeMap
	implements Accumulator<Node, Map<Node, Node>>
{
	// A map for e.g. int -> decimal - during addition transitivity is resolved
	// so {short -> decimal, int -> decimal, decimal -> decimal } rather than
	//    {short -> int, int -> decimal, decimal -> decimal }
	protected Map<Node, Node> weakerToMightierType;
	// protected transient LeastCommonAncestor alg = new LeastCommonAncestor(graph, gsf);

	protected BiFunction<? super Node, ? super Node, ? extends Node> lcaFinder;
	
	public AccRdfLiteralDatatypeTypeMap(BiFunction<? super Node, ? super Node, ? extends Node> lcaFinder) {
		super();
		this.weakerToMightierType = new LinkedHashMap<>();
		this.lcaFinder = lcaFinder;
	}
	
	@Override
	public void accumulate(Node input) {

		// Check whether the given input node is subsumed by any other node

		Node target = input;
		
		boolean changed = false;
		for (Entry<Node, Node> e : weakerToMightierType.entrySet()) {
			Node currentRemap = e.getValue();
						
			// Example:
			// Given: {(short, long), (int, long), (long, long)}
			// On accumulate(decimal): all longs become decimal
			// On accumulate(int): nothing happens, because long
			Node lca = lcaFinder.apply(currentRemap, input);
			if (lca != null) {
				if (!lca.equals(currentRemap)) {
					target = lca;
					changed = true;
					weakerToMightierType.entrySet().forEach(f -> {
						if (f.getValue().equals(currentRemap)) {
							f.setValue(lca);
						}
					});
				} else {
					target = currentRemap;
					break;
				}
			}
		}
		
		weakerToMightierType.put(input, target);
	}

	@Override
	public Map<Node, Node> getValue() {
		return weakerToMightierType;
	}
	
}
