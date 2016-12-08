package org.aksw.jena_sparql_api.mapper.context;

import org.aksw.jena_sparql_api.mapper.model.RdfType;
import org.apache.jena.graph.Node;

public class TypedNode {
	protected RdfType rdfType;
	protected Node node;

	public TypedNode(RdfType rdfType, Node node) {
		super();
		if(node == null) {
		    throw new NullPointerException();
		}

		this.rdfType = rdfType;
		this.node = node;
	}

	public RdfType getRdfType() {
		return rdfType;
	}

	public Node getNode() {
		return node;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((node == null) ? 0 : node.hashCode());
		result = prime * result + ((rdfType == null) ? 0 : rdfType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TypedNode other = (TypedNode) obj;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		if (rdfType == null) {
			if (other.rdfType != null)
				return false;
		} else if (!rdfType.equals(other.rdfType))
			return false;
		return true;
	}

    @Override
    public String toString() {
        return "TypedNode [rdfType=" + rdfType + ", node=" + node + "]";
    }
	
	
}
