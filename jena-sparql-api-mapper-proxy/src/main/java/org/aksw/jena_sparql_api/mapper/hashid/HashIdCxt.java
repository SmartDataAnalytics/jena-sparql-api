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

    // TODO Consider using ImmutableMap?
    Map<RDFNode, HashCode> getMapping();
}
