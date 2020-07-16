package org.aksw.jena_sparql_api.mapper.hashid;

import java.util.Map;
import java.util.function.BiFunction;

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
     * Declare a node to be visited.
     * Throws an exception if visited more than once without a hash being available.
     * I.e. the first visit should result in the computation of a hash code and not a further visit -
     * which indicates a loop.
     *
     *
     *
     *
     * @param node
     * @return
     */
    boolean declareVisit(RDFNode node);


    boolean isVisited(RDFNode node);

    HashCode putHash(RDFNode node, HashCode hashCode);
    HashCode getHash(RDFNode node);


    /**
     * Default approach for converting a HashCode into string representation, such as by applying
     * base64url encoding
     *
     * @param hashCode
     * @return
     */
    String getHashAsString(HashCode hashCode);
    String getHashAsString(RDFNode rdfNod);

    // Hash representation is generic, independent of strings and is always computed before applying
    // a string mapping
    // The purpose of strings is to allow for crafting nice IRIs
    // TODO It may be more flexible to allow for using an RDF model to capture hash codes, strings or other
    // pieces of information. However, then we would have to introduce new custom datatypes, such as
    // "0ab0c"^^eg:hexString
    String putString(RDFNode node, String id);
    String getString(RDFNode node);

    // TODO Consider using ImmutableMap - or even an RDF model?
    Map<RDFNode, HashCode> getMapping();
    Map<RDFNode, String> getStringMapping();
}
