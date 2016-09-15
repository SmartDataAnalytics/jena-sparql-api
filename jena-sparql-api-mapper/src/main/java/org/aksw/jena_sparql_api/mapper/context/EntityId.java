package org.aksw.jena_sparql_api.mapper.context;

import org.apache.jena.graph.Node;

public class EntityId {
	protected Class<?> entityClass;
	protected Node node;
	
	public EntityId(Class<?> entityClass, Node node) {
		super();
		this.entityClass = entityClass;
		this.node = node;
	}

	public Class<?> getEntityClass() {
		return entityClass;
	}
	
	public Node getNode() {
		return node;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entityClass == null) ? 0 : entityClass.hashCode());
		result = prime * result + ((node == null) ? 0 : node.hashCode());
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
		EntityId other = (EntityId) obj;
		if (entityClass == null) {
			if (other.entityClass != null)
				return false;
		} else if (!entityClass.equals(other.entityClass))
			return false;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "EntityId [entityClass=" + entityClass + ", node=" + node + "]";
	}
}
