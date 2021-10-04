package org.aksw.jena_sparql_api.rdf.collections;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

public class NodeMappers {
    public static final NodeMapper<Node> PASSTHROUGH = new NodeMapperPassthrough();

    public static final NodeMapper<String> string = NodeMappers.from(String.class);

    public static final NodeMapper<Long> xlong = NodeMappers.from(Long.class);

    public static final NodeMapper<String> DEFAULT_URI_OR_STRING = new NodeMapperUriOrString(
            str -> str.startsWith("file:") || UrlValidator.getInstance().isValid(str)); // || EmailValidator.getInstance().isValid(str));

    public static final NodeMapper<String> uriString = new NodeMapperDelegating<>(String.class,
            Node::isURI, NodeFactory::createURI, Node::getURI);


    public static <T> NodeMapper<T> fromDatatypeIri(String datatypeIri) {
        TypeMapper typeMapper = TypeMapper.getInstance();
        RDFDatatype dtype = typeMapper.getSafeTypeByName(datatypeIri);
        NodeMapper<T> result = new NodeMapperFromRdfDatatype<>(dtype);
        return result;
    }

    public static <T> NodeMapper<T> from(Class<T> clazz) {
        TypeMapper typeMapper = TypeMapper.getInstance();
        NodeMapper<T> result = from(clazz, typeMapper);
        return result;
    }

    public static <T> NodeMapper<T> from(Class<T> clazz, TypeMapper typeMapper) {
        RDFDatatype dtype = typeMapper.getTypeByClass(clazz);

        NodeMapper<T> result = new NodeMapperFromRdfDatatype<>(dtype);
        return result;
    }
}