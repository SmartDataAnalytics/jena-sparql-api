package org.aksw.jena_sparql_api.mapper.impl.type;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;

public class RdfTypeNode
	extends RdfTypePrimitiveBase
{
	@Override
	public Class<?> getEntityClass() {
		return Node.class;
	}

	@Override
	public Node getRootNode(Object obj) {
		return (Node)obj;
	}

	@Override
	public boolean hasIdentity() {
		throw new RuntimeException("Method should not be called on simple types");
	}

	@Override
	public Object createJavaObject(RDFNode r) {
		return r.asNode();		
	}

}
