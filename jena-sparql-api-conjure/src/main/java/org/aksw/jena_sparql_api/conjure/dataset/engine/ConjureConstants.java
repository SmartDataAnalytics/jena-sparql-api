package org.aksw.jena_sparql_api.conjure.dataset.engine;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

public class ConjureConstants {
	public static final String PROV_PLACEHOLDER_URI = "http://conjure.org/procenance/placeholder";
	public static final Node PROV_PLACEHOLDER_NODE = NodeFactory.createURI(PROV_PLACEHOLDER_URI);

}
