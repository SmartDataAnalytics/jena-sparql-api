package org.aksw.jena_sparql_api.utils.model;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

public class NodeMapperFactory {
	public static final NodeMapper<Node> PASSTHROUGH = new NodeMapperPassthrough();

	
	public static final NodeMapper<String> string = NodeMapperFactory.from(String.class);
	
	
	public static final NodeMapper<String> DEFAULT_URI_OR_STRING = new NodeMapperUriOrString(
			str -> UrlValidator.getInstance().isValid(str)); // || EmailValidator.getInstance().isValid(str));
	
	public static final NodeMapper<String> uriString = new NodeMapperDelegating<>(String.class,
			Node::isURI, NodeFactory::createURI, Node::getURI); 

	public static <T> NodeMapper<T> from(Class<T> clazz) {
		TypeMapper typeMapper = TypeMapper.getInstance();
		NodeMapper<T> result = from(clazz, typeMapper);
		return result;
	}

	public static <T> NodeMapper<T> from(Class<T> clazz, TypeMapper typeMapper) {
		RDFDatatype dtype = typeMapper.getTypeByClass(clazz);

		NodeMapper<T> result = new NodeMapperRdfDatatype<>(dtype);
		return result;
	}
}