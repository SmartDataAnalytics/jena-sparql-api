package org.aksw.jena_sparql_api.collection.observable;

import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.vocabulary.RDF;

public class Snippet {
	public static void main(String[] args) {
		Node n = NodeFactory.createLiteral("test", TypeMapper.getInstance().getSafeTypeByName(RDF.langString.getURI()));
		System.out.println("Node: " + n.getLiteralDatatypeURI());
		System.out.println("Value: " + n);
	}
}

