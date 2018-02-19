package org.aksw.jena_sparql_api.mapper.impl.engine;

import java.util.HashSet;
import java.util.Set;

import org.aksw.jena_sparql_api.mapper.context.EntityId;
import org.aksw.jena_sparql_api.mapper.impl.type.EntityFragment;
import org.aksw.jena_sparql_api.mapper.impl.type.ResourceFragment;
import org.apache.jena.rdf.model.RDFNode;

public class EntityState {
	protected Object entity;
	protected RDFNode shapeResource;
	protected ResourceFragment resourceFragment;
	protected EntityFragment entityFragment;
	protected RDFNode currentResource;
	
	protected Set<EntityId> dependentEntityIds = new HashSet<>();
		
	public EntityState(Object entity, RDFNode shapeResource, RDFNode currentResource, ResourceFragment resourceFragment,
			EntityFragment entityFragment) {
		super();
		this.entity = entity;
		this.shapeResource = shapeResource;
		this.resourceFragment = resourceFragment;
		this.entityFragment = entityFragment;
		this.currentResource = currentResource;
	}

	public Object getEntity() {
		return entity;
	}
	
	public RDFNode getShapeResource() {
		return shapeResource;
	}
	
	public ResourceFragment getResourceFragment() {
		return resourceFragment;
	}
	
	public EntityFragment getEntityFragment() {
		return entityFragment;
	}

	public RDFNode getCurrentResource() {
		return currentResource;
	}

	public void setCurrentResource(RDFNode currentResource) {
		this.currentResource = currentResource;
	}
	
	
	public Set<EntityId> getDependentEntityIds() {
		return dependentEntityIds;
	}
	
	
}
