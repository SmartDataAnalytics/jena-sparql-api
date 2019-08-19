package org.aksw.jena_sparql_api.rdf.collections;

import java.util.Objects;

import org.aksw.jena_sparql_api.mapper.proxy.TypeDecider;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.util.ModelUtils;

public class RDFNodeMapperImpl<T>
	implements RDFNodeMapper<T>
{
	protected TypeMapper typeMapper;
	protected TypeDecider typeDecider;
	protected Class<T> viewClass;

	protected transient NodeMapper<T> nodeMapper;

	
	public RDFNodeMapperImpl(Class<T> viewClass, TypeMapper typeMapper, TypeDecider typeDecider) {
		super();
		this.typeMapper = typeMapper;
		this.typeDecider = typeDecider;
		this.viewClass = viewClass;

		this.nodeMapper = new NodeMapperFromTypeMapper<>(viewClass, typeMapper); //NodeMapperFactory.from(viewClass, typeMapper);
	}
	
	public boolean canMap(RDFNode rdfNode) {			
		Object tmp = toJava(rdfNode);
		boolean result = tmp != null;
		return result;
	}
	
	public T toJava(RDFNode rdfNode) {
		Objects.requireNonNull(rdfNode);
		Objects.requireNonNull(viewClass);
		
		Node n = rdfNode.asNode();
		
		T result;
		if(nodeMapper.canMap(n)) {
			result = nodeMapper.toJava(n);
		} else {
			Class<?> effectiveType;
			if(rdfNode.isResource()) {
				Resource r = rdfNode.asResource();
				effectiveType = ResourceUtils.getMostSpecificSubclass(r, viewClass, typeDecider);
				
				if(effectiveType == null) {
					// We could not obtain a more specific type that the one requested -
					// try the requested type as a fallback
					// NOTE This case happens, if a resource with a model x was added to a model y:
					// In this case, all triples and thus the type information is lost, so no more
					// specific type is found
					
					// If we could not obtain a specific type, and the request was
					// a super class of RDFNode/Resource, yield a generic RDFNode view
					if(viewClass.isAssignableFrom(Resource.class)) {
						effectiveType = RDFNode.class;
					}
				}
			} else {
				effectiveType = viewClass;
			}
			
			result = effectiveType == null
					? null
					: rdfNode.canAs((Class)effectiveType) 
						? (T)rdfNode.as((Class)effectiveType)
						: null;
		}

		return result;
	}

	@Override
	public Class<?> getJavaClass() {
		return viewClass;
	}

	@Override
	public RDFNode toNode(T obj) {
		RDFNode result;
		
		// If the view demands subclasses of RDFNode, use the type decider system
//		if(RDFNode.class.isAssignableFrom(viewClass) && obj instanceof Resource) {
		if(obj instanceof Resource) {
			Resource r = (Resource)obj;
			Class<?> effectiveViewClass = ResourceUtils.getMostSpecificSubclass(r, viewClass, typeDecider);
			
			// If we ended up with parent of RDFNode, constrain to RDFNode 
			if(effectiveViewClass.isAssignableFrom(RDFNode.class)) {
				effectiveViewClass = RDFNode.class;
			}
			
			// TODO If there are multiple types, we return null  for now
			// We could however under certain circumstances create a proxy that implements all types
			// (i.e. all but one types must be interfaces)
			result = effectiveViewClass == null ? null : r.as((Class)effectiveViewClass);
		} else {
			Node n = nodeMapper.toNode(obj);
			result = ModelUtils.convertGraphNodeToRDFNode(n);
		}
		
		return result;
	}

}