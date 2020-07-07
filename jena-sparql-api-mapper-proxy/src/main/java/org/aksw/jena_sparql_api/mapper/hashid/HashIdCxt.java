package org.aksw.jena_sparql_api.mapper.hashid;

import org.apache.jena.ext.com.google.common.hash.HashCode;
import org.apache.jena.rdf.model.RDFNode;

public interface HashIdCxt {
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


    boolean putHash(RDFNode node, HashCode hashCode);
    HashCode getHash(RDFNode node);
}
