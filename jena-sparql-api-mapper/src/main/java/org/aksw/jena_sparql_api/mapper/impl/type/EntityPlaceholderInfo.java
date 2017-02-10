package org.aksw.jena_sparql_api.mapper.impl.type;

import org.aksw.jena_sparql_api.beans.model.PropertyOps;
import org.aksw.jena_sparql_api.mapper.model.RdfMapperProperty;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

/**
 * Placeholder that contains information about how to resolve
 * an RDF node of another entity's RDF graph to an entity
 * 
 * @author raven
 *
 */
public class EntityPlaceholderInfo
	extends PlaceholderInfo
{
	protected RDFNode rdfNode;
	protected Class<?> targetClass;
	
	public EntityPlaceholderInfo(Class<?> targetClass, Object parentEntity, Resource parentRes, PropertyOps propertyOps, RDFNode rdfNode,
			RdfMapperProperty mapper) {
		super(targetClass, null, parentEntity, propertyOps, null, mapper, null);

		this.targetClass = targetClass;
		this.rdfNode = rdfNode;
	}

//	public Class<?> getTargetClass() {
//		return targetClass;
//	}

	public RDFNode getRdfNode() {
		return rdfNode;
	}
	
	
}
