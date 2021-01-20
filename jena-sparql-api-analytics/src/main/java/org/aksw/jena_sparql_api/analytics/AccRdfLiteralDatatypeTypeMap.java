package org.aksw.jena_sparql_api.analytics;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.mapper.Accumulator;
import org.aksw.jena_sparql_api.util.graph.alg.BreadthFirstSearchLib;
import org.aksw.jena_sparql_api.util.graph.alg.GraphSuccessorFunction;
import org.aksw.jena_sparql_api.util.graph.alg.LeastCommonAncestor;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

/**
 * 
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

	// Do not traverse through capping types when checking the type hierarchy
	protected Set<Node> cappingTypes = Collections.singleton(NodeFactory.createURI(XSD.NS + "anyAtomicType"));

	protected LeastCommonAncestor alg;
	
	public AccRdfLiteralDatatypeTypeMap(Map<Node, Node> state) {
		super();
		this.weakerToMightierType = state;

		Model model = RDFDataMgr.loadModel("xsd-ontology.ttl");
		
		Graph graph = model.getGraph();
		GraphSuccessorFunction gsf = GraphSuccessorFunction.create(RDFS.subClassOf.asNode(), true);
		
		// Filter out capping types from the successors
		this.alg = new LeastCommonAncestor(graph, (n, g) -> gsf.apply(n, g).filter(m -> !cappingTypes.contains(m)));

	}
	
	@Override
	public void accumulate(Node input) {

		// Check whether the given input node is subsumed by any other node
		
		Map<Node, Node> newState = new LinkedHashMap<>();
		for (Node key : weakerToMightierType.keySet()) {
			
			Set<Node> commonAncestors = alg.leastCommonAncestors(key, input);

			if (commonAncestors.size() > 1) {
				throw new RuntimeException("Should not happen");
			} else {
				Node ca = commonAncestors.isEmpty() ? null : commonAncestors.iterator().next();
				
				weakerToMightierType.put(key, ca);
				weakerToMightierType.put(input, ca);
			}
			
		}
		
//		Stream<Set<Node>> breadthOfParentsStream = BreadthFirstSearchLib.stream(expected, node -> gsf.apply(graph, node), Collectors::toSet);

	}

	@Override
	public Map<Node, Node> getValue() {
		// TODO Auto-generated method stub
		return null;
	}

}
