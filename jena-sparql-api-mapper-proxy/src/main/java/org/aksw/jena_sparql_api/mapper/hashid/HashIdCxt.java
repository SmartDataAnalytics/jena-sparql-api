package org.aksw.jena_sparql_api.mapper.hashid;

import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.RDFNode;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;

public interface HashIdCxt {
    BiFunction<? super RDFNode, ? super HashIdCxt, ? extends HashCode> getGlobalProcessor();

    /**
     * The hashing used in this context
     *
     * @return
     */
    HashFunction getHashFunction();

    /**
     * Declare a node to be in processing state.
     * Throws an exception if invoked more than once without a hash being available.
     * I.e. the first visit should result in the computation of a hash code and not a further visit -
     * which indicates a loop.
     *
     * TODO requestProcessing(State) may be a better name - as the request may fail.
     *
     *
     * @param node
     * @return
     */
    boolean declareProcessing(RDFNode node);

    /**
     * Declare traversal of a node. Traversal is the search for nodes subject to processing.
     * A node may be declared as traversed any number of times - in contrast to declareProcessing
     * which may only be invoked once on an unprocessed node.
     *
     * @param node
     * @return
     */
    boolean declarePending(RDFNode node);
    boolean isPending(RDFNode node);
    Set<RDFNode> getPending();

    boolean isVisited(RDFNode node);


    HashCode putHashId(RDFNode node, HashCode hashCode);
    HashCode getHashId(RDFNode node);


    /**
     * Default approach for converting a HashCode into string representation, such as by applying
     * base64url encoding
     *
     * @param hashCode
     * @return
     */
    String getHashAsString(HashCode hashCode);
    String getHashAsString(RDFNode rdfNode);

    // Hash representation is generic, independent of strings and is always computed before applying
    // a string mapping
    // The purpose of strings is to allow for crafting nice IRIs
    // TODO It may be more flexible to allow for using an RDF model to capture hash codes, strings or other
    // pieces of information. However, then we would have to introduce new custom datatypes, such as
    // "0ab0c"^^eg:hexString
    String putStringId(RDFNode node, String id);
    String getStringId(RDFNode node);

    // TODO Consider using ImmutableMap - or even an RDF model?
    Map<RDFNode, HashCode> getHashIdMapping();
    Map<RDFNode, String> getStringIdMapping();
    
    
    /**
     * Convenience method that transforms the result of {@link #getStringIdMapping()} such that keys are Nodes.
     * Useful for applying renaming.
     */
    default Map<Node, Node> getNodeMapping(String baseIri) {
    	Map<RDFNode, String> baseMap = getStringIdMapping();
    	
    	Map<Node, Node> result = baseMap.entrySet().stream()
    			.collect(Collectors.toMap(
    					e -> e.getKey().asNode(),
    					e -> NodeFactory.createURI(baseIri + e.getValue())));

    	return result;
    }
    
}
