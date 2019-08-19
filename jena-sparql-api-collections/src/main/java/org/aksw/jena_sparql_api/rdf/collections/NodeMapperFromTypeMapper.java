package org.aksw.jena_sparql_api.rdf.collections;

import java.util.Objects;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

public class NodeMapperFromTypeMapper<T>
	implements NodeMapper<T>
{
	protected TypeMapper typeMapper;
	protected Class<?> viewClass;

	
	// Base class for acceptable literal types - this only acts as a filter
	public NodeMapperFromTypeMapper(Class<?> viewClass, TypeMapper typeMapper) {
		this.viewClass = viewClass;
		this.typeMapper = typeMapper;
	}
	
	@Override
	public Class<?> getJavaClass() {
		return viewClass;
	}
	
	@Override
	public boolean canMap(Node node) {
		boolean result = NodeMapperFromRdfDatatype.canMapCore(node, viewClass);
//		
//		boolean result = node.isLiteral();
//		if(node.isLiteral()) {
//			Class<?> literalClass = node.getLiteral().getDatatype().getJavaClass();
//			result = literalClass != null && viewClass.isAssignableFrom(literalClass);
//		} else {
//			result = false;
//		}

		return result;
	}

	@Override
	public T toJava(Node node) {
		Object result = NodeMapperFromRdfDatatype.toJavaCore(node, viewClass);
		return (T)result;
		//return (T)node.getLiteralValue();
	}

	@Override
	public Node toNode(T obj) {
		RDFDatatype dtype = typeMapper.getTypeByValue(obj);
		Objects.requireNonNull(dtype);

//		String lex = dtype.unparse(obj);
		//Node result = NodeFactory.createLiteral(lex, dtype);
		Node result = NodeFactory.createLiteralByValue(obj, dtype);

		return result;
		
//		Node result;
//
//		RDFDatatype dtype = typeMapper.getTypeByValue(obj);
//		if(dtype != null) {
//			result = NodeFactory.createLiteralByValue(obj, dtype);
//		} else {
//			result = null;
//		}
//		
//		// TODO Auto-generated method stub
//		return result;
	}

}
