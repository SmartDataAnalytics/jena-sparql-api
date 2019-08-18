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

		this.nodeMapper = new NodeMapperFromTypeMapper<>(viewClass); //NodeMapperFactory.from(viewClass, typeMapper);
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
		if(RDFNode.class.isAssignableFrom(viewClass) && obj instanceof Resource) {
			Resource r = (Resource)obj;
			Class<?> effectiveViewClass = ResourceUtils.getMostSpecificSubclass(r, viewClass, typeDecider);
			
			result = r.as((Class)effectiveViewClass);
		} else {
			Node n = nodeMapper.toNode(obj);
			result = ModelUtils.convertGraphNodeToRDFNode(n);
		}
		
		return result;
	}

}