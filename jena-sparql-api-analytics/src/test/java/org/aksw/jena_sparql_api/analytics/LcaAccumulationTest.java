package org.aksw.jena_sparql_api.analytics;

import java.util.Collections;
import java.util.Set;

import org.aksw.jena_sparql_api.util.graph.alg.GraphSuccessorFunction;
import org.aksw.jena_sparql_api.util.graph.alg.NaiveLCAFinder;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.junit.Test;

public class LcaAccumulationTest {
	@Test
	public void test() {
		Model model = RDFDataMgr.loadModel("xsd-ontology.ttl");
		
		Graph graph = model.getGraph();
		GraphSuccessorFunction gsf = GraphSuccessorFunction.create(RDFS.subClassOf.asNode(), true);
		Set<Node> cappingTypes = Collections.singleton(NodeFactory.createURI(XSD.NS + "anyAtomicType"));
		// Set<Node> cappingTypes = Collections.emptySet();
		NaiveLCAFinder lcaFinder = new NaiveLCAFinder(graph, (n, g) -> gsf.apply(n, g).filter(m -> !cappingTypes.contains(m)));

		AccRdfLiteralDatatypeTypeMap acc = new AccRdfLiteralDatatypeTypeMap(lcaFinder::getLCA);

		acc.accumulate(XSD.xdouble.asNode());
		acc.accumulate(XSD.xint.asNode());
		acc.accumulate(XSD.xshort.asNode());
		acc.accumulate(XSD.decimal.asNode());
		acc.accumulate(XSD.xlong.asNode());
		acc.accumulate(XSD.xstring.asNode());
		acc.accumulate(NodeFactory.createURI(XSD.NS + "anyType"));
		
		System.out.println(acc.getValue());
	}

}
