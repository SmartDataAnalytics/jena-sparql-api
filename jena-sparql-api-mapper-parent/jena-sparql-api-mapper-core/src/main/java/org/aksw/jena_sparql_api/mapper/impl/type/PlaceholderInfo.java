package org.aksw.jena_sparql_api.mapper.impl.type;

import java.util.Map;
import java.util.function.Function;

import org.aksw.commons.beans.model.PropertyOps;
import org.aksw.jena_sparql_api.mapper.model.RdfMapperProperty;
import org.aksw.jena_sparql_api.mapper.model.RdfType;
import org.aksw.jena_sparql_api.mapper.util.ValueHolder;
import org.apache.jena.rdf.model.RDFNode;

/**
 * 
 * 
 * @author raven
 *
 */
public class PlaceholderInfo {
	// TODO Maybe this should be only a reference to the targetRdfType, instead of that object itself
	protected Class<?> targetClass;
	protected RdfType targetRdfType;

	protected Object parentEntity;
	protected RDFNode parentRdfNode;
	protected PropertyOps propertyOps;
	
	protected Object value;
	protected RDFNode rdfNode;

	
	// Function to generate an IRI for the placeholder based on the parent's IRI
	//protected BiFunction<String, Object, String> generateIriFn;

	// The mapper that generated this placeholder information
	protected RdfMapperProperty mapper;
	
	protected ValueHolder valueHolder;	
	
	/**
	 * Function that returns a node's URI based on a remapping of
	 * of nodes in the fragment that were resolved.
	 * 
	 */
	protected Function<Map<RDFNode, RDFNode>, RDFNode> iriGenerator;
	
	public PlaceholderInfo(Class<?> targetClass, RdfType targetRdfType, Object parentEntity, RDFNode parentRdfNode, PropertyOps propertyOps, Object value,
			RDFNode rdfNode, RdfMapperProperty mapper) {// ValueHolder valueHolder) {
		super();
		this.targetClass = targetClass;
		this.targetRdfType = targetRdfType;
		this.parentEntity = parentEntity;
		this.parentRdfNode = parentRdfNode;
		this.propertyOps = propertyOps;
		this.value = value;
		this.rdfNode = rdfNode;
		this.mapper = mapper;
		//this.valueHolder = valueHolder;
	}

	public Class<?> getTargetClass() {
		return targetClass;
	}

	public RdfType getTargetRdfType() {
		return targetRdfType;
	}

	public Object getParentEntity() {
		return parentEntity;
	}

	public RDFNode getParentRdfNode() {
		return parentRdfNode;
	}
	
	public PropertyOps getPropertyOps() {
		return propertyOps;
	}

	public Object getValue() {
		return value;
	}

	public RDFNode getRdfNode() {
		return rdfNode;
	}

	public RdfMapperProperty getMapper() {
		return mapper;
	}
	
	public Function<Map<RDFNode, RDFNode>, RDFNode> getIriGenerator() {
		return iriGenerator;
	}

	@Override
	public String toString() {
		return "PlaceholderInfo [targetRdfType=" + targetRdfType + ", parentEntity=" + parentEntity + ", propertyName="
				+ propertyOps + ", value=" + value + ", mapper=" + mapper + "]";
	}
	
}
