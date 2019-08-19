package org.aksw.jena_sparql_api.rdf.collections;

import java.util.function.Predicate;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.vocabulary.XSD;

/**
 * Node mapper backed some best-effort method to
 * 
 * @author raven Apr 10, 2018
 *
 */
public class NodeMapperUriOrString
	implements NodeMapper<String>
{
	protected Predicate<String> testForUri;
	
	public NodeMapperUriOrString(Predicate<String> testForUri) {
		super();
		this.testForUri = testForUri;
	}

	@Override
	public Class<?> getJavaClass() {
		return String.class;
	}

	@Override
	public boolean canMap(Node node) {
		boolean result = node.isURI() ||
				(node.isLiteral() && 
						XSD.xstring.toString().equals(node.getLiteralDatatypeURI()));
				
		return result;
	}

	@Override
	public Node toNode(String str) {
		boolean isUri = testForUri.test(str);
		Node result = isUri ? NodeFactory.createURI(str) : NodeFactory.createLiteral(str);
		return result;
	}

	@Override
	public String toJava(Node node) {
		return node.isURI() ? node.getURI() : node.getLiteralLexicalForm();
	}

}
