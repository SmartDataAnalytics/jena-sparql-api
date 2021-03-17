package org.aksw.jena_sparql_api.schema_mapping;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collector.core.AggBuilder;
import org.aksw.commons.collector.core.AggLcaMap;
import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.collector.domain.Aggregator;
import org.aksw.jena_sparql_api.util.graph.alg.BreadthFirstSearchLib;
import org.aksw.jena_sparql_api.util.graph.alg.GraphSuccessorFunction;
import org.aksw.jena_sparql_api.util.graph.alg.NaiveLCAFinder;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

public class TypePromoterImpl
	implements TypePromoter
{
	protected Aggregator<String, Map<String, String>> typeAggregator;
	
	public TypePromoterImpl(Aggregator<String, Map<String, String>> typeAggregator) {
		super();
		this.typeAggregator = typeAggregator;
	}


	@Override
	public Map<String, String> promoteTypes(Set<String> datatypeIris) {

		Accumulator<String, Map<String, String>> acc = typeAggregator.createAccumulator();

		for (String datatypeIri : datatypeIris) {
			acc.accumulate(datatypeIri);
		}
		
		Map<String, String> actual = acc.getValue(); 

		Map<String, RDFDatatype> targetTypes = actual.values().stream()
				.distinct()
				.map(node -> new SimpleEntry<>(node, pullUpType(node)))
				.filter(e -> e.getValue() != null)
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));

		Map<String, RDFDatatype> mapping = actual.entrySet().stream()
				.map(kv -> new SimpleEntry<>(kv.getKey(), targetTypes.get(kv.getValue())))
				.filter(e -> e.getValue() != null)
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
				
		
		Map<String, String> result = mapping.entrySet().stream()
				.collect(Collectors.toMap(
						Entry::getKey,
						e -> e.getValue().getURI()));
		
		Set<String> failedToMap = Sets.difference(actual.keySet(), mapping.keySet());

		boolean ignoreUnknownTypes = true;
		
		if (!ignoreUnknownTypes) {
			if (!failedToMap.isEmpty()) {
				throw new RuntimeException("Failed to map: " + failedToMap);
			}
		}
		
		return result;
	}
	

	public static TypePromoter create() {
		Model model = RDFDataMgr.loadModel("xsd-ontology.ttl");		
		Graph graph = model.getGraph();
		GraphSuccessorFunction gsf = GraphSuccessorFunction.create(RDFS.subClassOf.asNode(), true);
		Set<Node> cappingTypes = Collections.singleton(NodeFactory.createURI(XSD.NS + "anyAtomicType"));
		// Set<Node> cappingTypes = Collections.emptySet();
		NaiveLCAFinder lcaFinder = new NaiveLCAFinder(graph, (n, g) -> gsf.apply(n, g).filter(m -> !cappingTypes.contains(m)));

		// The core aggregator is based on IRI Nodes - wrap it for String
		Aggregator<String, Map<String, String>> typeAggregator =
		AggBuilder.outputTransform(
			AggBuilder.inputTransform(NodeFactory::createURI,
				AggLcaMap.create(lcaFinder::getLCA)),			
			(Map<Node, Node> map)->
			map.entrySet().stream().collect(Collectors.toMap(
					e -> e.getKey().getURI(),
					e -> e.getValue().getURI()))
			);
				
		TypePromoterImpl result = new TypePromoterImpl(typeAggregator);
		return result;
	}

	
	public static RDFDatatype pullUpType(String datatypeIri) {
		return pullUpType(NodeFactory.createURI(datatypeIri));
	}

	public static RDFDatatype pullUpType(Node datatype) {
		return pullUpType(Collections.singleton(datatype));
	}

	/**
	 * Given a set of starting types (wrapped as Nodes) and a backing type hierarchy,
	 * use breadth first search to find the first set of ancestor types which contain at least one
	 * type with a mapping to a Java datatypes w.r.t. Jena's {@link TypeMapper}.
	 * 
	 * @param start The set of string types
	 * @return
	 */
	public static RDFDatatype pullUpType(Set<Node> start) {
		Model model = RDFDataMgr.loadModel("xsd-ontology.ttl");		
		Graph graph = model.getGraph();
		GraphSuccessorFunction gsf = GraphSuccessorFunction.create(RDFS.subClassOf.asNode(), true);

		TypeMapper tm = TypeMapper.getInstance();
		
		Stream<Set<Node>> breadthOfParentsStream = BreadthFirstSearchLib.stream(start, node -> gsf.apply(graph, node), Collectors::toSet);

		
		Map<Node, RDFDatatype> javaTypeMap = breadthOfParentsStream.map(set -> {
			Map<Node, RDFDatatype> map = set.stream()
				.map(node -> Maps.immutableEntry(node, tm.getTypeByName(node.getURI())))
				.filter(e -> e.getValue() != null && e.getValue().getJavaClass() != null)
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
			return map;
		})
		.filter(map -> !map.isEmpty())
		.findFirst().orElse(null);

		
		RDFDatatype result;
		if (javaTypeMap == null) {
			result = null;
		} else {
			result = Iterables.getOnlyElement(javaTypeMap.values());
		}
		
//		System.out.println("res: " + result);	
		return result;
	}
}
